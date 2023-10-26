package kubeiaas.iaascore.process;

import kubeiaas.common.bean.IpSegment;
import kubeiaas.common.bean.IpUsed;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.bean.IpSegmentConstants;
import kubeiaas.common.constants.bean.IpUsedConstants;
import kubeiaas.common.enums.network.IpAttachEnum;
import kubeiaas.common.enums.network.IpTypeEnum;
import kubeiaas.common.utils.EnumUtils;
import kubeiaas.common.utils.IpUtils;
import kubeiaas.common.utils.MacUtils;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.VmException;
import kubeiaas.iaascore.request.vm.SetPasswdForm;
import kubeiaas.iaascore.scheduler.DhcpScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NetworkProcess {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private DhcpScheduler dhcpScheduler;

    /**
     * Create VM.
     * 3. Network
     * use synchronized to ensure synchronous execution, avoid ip allocation conflicts.
     */
    public synchronized IpUsed createVmNetwork(Vm newVm, int ipSegmentId) throws VmException {
        log.debug("createVm -- 3. Network");
        log.info("-> invoke DB -- ipSegmentQueryById");
        IpSegment ipSegment = tableStorage.ipSegmentQueryById(ipSegmentId);
        log.info("<- invoke DB -- done");
        String newMac = getNewMac(ipSegment);
        IpUsed newIpUsed = getNewIp(ipSegment);
        if (newIpUsed == null) {
            log.error(String.format("ERROR: createVmNetwork error"));
            throw new VmException(newVm, "ERROR: ip allocated failed!");
        }

        // already set: ip, ip_segment_id, type.
        newIpUsed.setMac(newMac);
        newIpUsed.setInstanceUuid(newVm.getUuid());
        newIpUsed.setCreateTime(new Timestamp(System.currentTimeMillis()));
        newIpUsed.setStatus(IpAttachEnum.DETACHED);
        newIpUsed.setType(ipSegment.getType());
        newIpUsed.setBridge(ipSegment.getBridge());

        log.info(String.format("new mac: %s", newIpUsed.getMac()));
        log.info(String.format("new ip: %s", newIpUsed.getIp()));

        // save into DB
        log.info("-> invoke DB -- ipUsedSave");
        newIpUsed = tableStorage.ipUsedSave(newIpUsed);
        log.info("<- invoke DB -- done");

        // bind in DHCP-Controller
        if (!dhcpScheduler.bindMacAndIp(newIpUsed)) {
            log.error(String.format("ERROR: dhcp bind mac & ip failed!"));
            throw new VmException(newVm, "ERROR: dhcp bind mac & ip failed!");
        }
        log.debug("createVm -- 3. network success!");
        return newIpUsed;
    }

    /**
     * save IpSegment
     */
    public IpSegment saveIpSegment(
            IpSegment ipSegment,
            String name,
            String hostUuid,
            String type,
            String bridge,
            String ipRangeStart,
            String ipRangeEnd,
            String gateway,
            String netmask) {
        // 1. update DB
        ipSegment.setName(name);
        ipSegment.setHostUuid(hostUuid);
        ipSegment.setBridge(bridge);
        ipSegment.setDns("");
        ipSegment.setIpRangeStart(ipRangeStart);
        ipSegment.setIpRangeEnd(ipRangeEnd);
        ipSegment.setGateway(gateway);
        ipSegment.setNetmask(netmask);
        ipSegment.setType(EnumUtils.getEnumFromString(IpTypeEnum.class, type));
        log.info("-> invoke DB -- ipSegmentSave");
        ipSegment = tableStorage.ipSegmentSave(ipSegment);
        log.info("<- invoke DB -- done");

        // 2. update DHCP config
        log.info("-> invoke dhcpScheduler -- updateIpSeg");
        dhcpScheduler.updateIpSeg(String.valueOf(ipSegment.getId()));
        log.info("<- invoke dhcpScheduler -- done");

        return ipSegment;
    }

    /**
     * Delete IP
     */
    public void deleteIps(String vmUuid) throws BaseException {

        // DHCP中解绑
        if (!dhcpScheduler.unbindMacAndIp(vmUuid)){
            log.error(String.format("ERROR: dhcp unbind failed!"));
            throw new BaseException("ERROR: dhcp unbind failed!");
        }

        // 数据库中删除
        log.info("-> invoke DB -- ipUsedDeleteByVmUuid");
        tableStorage.ipUsedDeleteByVmUuid(vmUuid);
        log.info("<- invoke DB -- done");
    }

    // =================================================================================================================

    /**
     * 获取 Total IP Seg 总量
     */
    public Integer getAllTotalNum(IpTypeEnum type) {
        int count = 0;
        log.info("-> invoke DB -- ipSegmentQueryAll");
        List<IpSegment> ipSegmentList = tableStorage.ipSegmentQueryAll();
        log.info("<- invoke DB -- done");
        for (IpSegment ipSegment : ipSegmentList) {
            if (ipSegment.getType().equals(type)) {
                count += IpUtils.getTotalIpNum(ipSegment);
            }
        }
        return count;
    }

    /**
     * 获取 Total IP Seg 用量
     */
    public Integer getAllUsedNum(IpTypeEnum type) {
        int count = 0;
        log.info("-> invoke DB -- ipSegmentQueryAll");
        List<IpSegment> ipSegmentList = tableStorage.ipSegmentQueryAll();
        log.info("<- invoke DB -- done");
        for (IpSegment ipSegment : ipSegmentList) {
            if (ipSegment.getType().equals(type)) {
                count += getAllUsedIp(ipSegment.getId()).size();
            }
        }
        return count;
    }

    /**
     * 获取 SegId IP Seg 总量
     */
    public Integer getAllNumBySegId(Integer id) throws BaseException {
        log.info("-> invoke DB -- ipSegmentQueryById");
        IpSegment ipSegment = tableStorage.ipSegmentQueryById(id);
        log.info("<- invoke DB -- done");
        if (ipSegment == null) {
            log.error(String.format("ERROR: ipSegmentId illegal"));
            throw new BaseException("err: ipSegmentId illegal");
        }
        return IpUtils.getTotalIpNum(ipSegment);
    }

    /**
     * 获取 SegId IP Seg 用量
     */
    public Integer getAllUsedNumBySegId(Integer id) throws BaseException {
        log.info("-> invoke DB -- ipSegmentQueryById");
        IpSegment ipSegment = tableStorage.ipSegmentQueryById(id);
        log.info("<- invoke DB -- done");
        if (ipSegment == null) {
            log.error(String.format("ERROR: ipSegmentId illegal"));
            throw new BaseException("err: ipSegmentId illegal");
        }
        return getAllUsedIp(ipSegment.getId()).size();
    }

    // 获取分配 MAC 地址
    public String getNewMac(IpSegment ipSegment) {
        if (ipSegment == null) {
            return IpUsedConstants.DEFAULT_MAC;
        }
        String macPre = MacUtils.getMacPre(ipSegment.getId(), ipSegment.getType());
        return MacUtils.getMACAddress(macPre);
    }

    // 获取分配 IP 地址
    public IpUsed getNewIp(IpSegment ipSegment) {
        if (ipSegment == null) {
            return null;
        }
        return IpUtils.getIpAddress(getAllUsedIp(ipSegment.getId()), ipSegment);
    }

    // 已经使用的ip地址，通过Map方式存放，key是Ip
    public Map<String, IpUsed> getAllUsedIp(int ipSegmentId) {
        log.info("-> invoke DB -- ipUsedQueryAllByIpSegmentId");
        List<IpUsed> ipUsedList = tableStorage.ipUsedQueryAllByIpSegmentId(ipSegmentId);
        log.info("<- invoke DB -- done");
        Map<String, IpUsed> allUsedIp = new HashMap<>();
        for (IpUsed ipUsed : ipUsedList) {
            allUsedIp.put(ipUsed.getIp(), ipUsed);
        }
        return allUsedIp;
    }

}
