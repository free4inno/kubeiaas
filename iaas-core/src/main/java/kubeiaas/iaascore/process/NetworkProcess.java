package kubeiaas.iaascore.process;

import kubeiaas.common.bean.IpSegment;
import kubeiaas.common.bean.IpUsed;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.bean.IpUsedConstants;
import kubeiaas.common.enums.network.IpAttachEnum;
import kubeiaas.common.enums.network.IpTypeEnum;
import kubeiaas.common.utils.IpUtils;
import kubeiaas.common.utils.MacUtils;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.VmException;
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
        log.info("createVm -- 3. Network");

        IpSegment ipSegment = tableStorage.ipSegmentQueryById(ipSegmentId);

        String newMac = getNewMac(ipSegment);
        IpUsed newIpUsed = getNewIp(ipSegment);
        if (newIpUsed == null) {
            throw new VmException(newVm, "ERROR: ip allocated failed!");
        }

        // already set: ip, ip_segment_id, type.
        newIpUsed.setMac(newMac);
        newIpUsed.setInstanceUuid(newVm.getUuid());
        newIpUsed.setCreateTime(new Timestamp(System.currentTimeMillis()));
        newIpUsed.setStatus(IpAttachEnum.DETACHED);
        newIpUsed.setType(IpTypeEnum.PRIVATE);
        newIpUsed.setBridge(ipSegment.getBridge());

        log.info("new mac: " + newIpUsed.getMac());
        log.info("new ip: " + newIpUsed.getIp());

        // save into DB
        newIpUsed = tableStorage.ipUsedSave(newIpUsed);

        // bind in DHCP-Controller
        if (!dhcpScheduler.bindMacAndIp(newIpUsed)) {
            throw new VmException(newVm, "ERROR: dhcp bind mac & ip failed!");
        }
        log.info("createVm -- 3. network success!");
        return newIpUsed;
    }


    public String getNewMac(IpSegment ipSegment) {
        if (ipSegment == null) {
            return IpUsedConstants.DEFAULT_MAC;
        }
        String macPre = MacUtils.getMacPre(ipSegment.getId(), ipSegment.getType());
        return MacUtils.getMACAddress(macPre);
    }


    public IpUsed getNewIp(IpSegment ipSegment) {
        if (ipSegment == null) {
            return null;
        }
        return IpUtils.getIpAddress(getAllUsedIp(ipSegment.getId()), ipSegment);
    }


    // 已经使用的ip地址，通过Map方式存放，key是Ip
    public Map<String, IpUsed> getAllUsedIp(int ipSegmentId) {
        List<IpUsed> ipUsedList = tableStorage.ipUsedQueryAllByIpSegmentId(ipSegmentId);
        Map<String, IpUsed> allUsedIp = new HashMap<>();
        for (IpUsed ipUsed : ipUsedList) {
            allUsedIp.put(ipUsed.getIp(), ipUsed);
        }
        return allUsedIp;
    }

    public void deleteIps(String vmUuid) throws BaseException {
        log.info("deleteIps ==== start ====  vmUuid: " + vmUuid);

        //DHCP中解绑
        if (!dhcpScheduler.unbindMacAndIp(vmUuid)){
            throw new BaseException("ERROR: dhcp unbind failed!");
        }

        //数据库中删除
        tableStorage.ipUsedDeleteByVmUuid(vmUuid);
        log.info("deleteIps ==== end ====");
    }
}
