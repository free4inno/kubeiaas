package kubeiaas.iaascore.service;

import kubeiaas.common.bean.*;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.IpSegmentConstants;
import kubeiaas.common.enums.network.IpTypeEnum;
import kubeiaas.common.utils.IpUtils;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.process.NetworkProcess;
import kubeiaas.iaascore.response.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NetworkService {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private NetworkProcess networkProcess;

    /**
     * 获取网段列表（全部）
     */
    public List<IpSegment> queryAllIpSeg() {
        // 建立 uuid -> name 的索引
        Map<String, String> hostUuidNameMap = new HashMap<>();
        log.info("-> invoke DB -- hostQueryAll");
        List<Host> hostList = tableStorage.hostQueryAll();
        log.info("<- invoke DB -- done");
        for (Host host : hostList) {
            hostUuidNameMap.put(host.getUuid(), host.getName());
        }

        List<HostBridge> hostBridgeList = tableStorage.hostBridgeQueryAll();
        //set hostBridge hostName
        for (HostBridge hostBridge : hostBridgeList) {
            String hostName = hostUuidNameMap.get(hostBridge.getHostUuid());
            hostBridge.setHostName(hostName);
            tableStorage.hostBridgeSave(hostBridge);
        }

        log.info("-> invoke DB -- ipSegmentQueryAll");
        List<IpSegment> ipSegmentList = tableStorage.ipSegmentQueryAll();
        log.info("<- invoke DB -- done");
        return ipSegmentList;
    }

    public List<HostBridge> queryAllHostBridge() {
        log.info("-> invoke DB -- hostBridgeQueryAll");
        List<HostBridge> hostBridgeList = tableStorage.hostBridgeQueryAll();
        log.info("<- invoke DB -- done");
        return hostBridgeList;
    }

    /**
     * statistics
     */
    public Map<String, Integer> getIpCount() {
        Map<String, Integer> resMap = new HashMap<>();
        // 1. 私网IP - 总量
        Integer privateIpTotal = networkProcess.getAllTotalNum(IpTypeEnum.PRIVATE);
        resMap.put(IpTypeEnum.PRIVATE + "_" + IpSegmentConstants.TOTAL, privateIpTotal);
        // 2. 私网IP - 用量
        Integer privateIpUsed = networkProcess.getAllUsedNum(IpTypeEnum.PRIVATE);
        resMap.put(IpTypeEnum.PRIVATE + "_" + IpSegmentConstants.USED, privateIpUsed);
        // 3. 公网IP - 总量
        Integer publicIpTotal = networkProcess.getAllTotalNum(IpTypeEnum.PUBLIC);
        resMap.put(IpTypeEnum.PUBLIC + "_" + IpSegmentConstants.TOTAL, publicIpTotal);
        // 4. 公网IP - 用量
        Integer publicIpUsed = networkProcess.getAllUsedNum(IpTypeEnum.PUBLIC);
        resMap.put(IpTypeEnum.PUBLIC + "_" + IpSegmentConstants.USED, publicIpUsed);

        return resMap;
    }

    public Map<String, Integer> getIpCount(Integer segId) throws BaseException {
        log.info(String.format("getIpCount info -- segId: %d", segId));

        Map<String, Integer> resMap = new HashMap<>();

        // 1. 总量
        Integer total = networkProcess.getAllNumBySegId(segId);
        resMap.put(IpSegmentConstants.TOTAL, total);

        // 2. 用量
        Integer used = networkProcess.getAllUsedNumBySegId(segId);
        resMap.put(IpSegmentConstants.USED, used);

        return resMap;
    }

    /**
     * 新建编辑
     */
    public IpSegment updateIpSegment(
            Integer ipSegmentId,
            String name,
//            String hostUuid,
            String type,
//            String bridge,
            String ipRangeStart,
            String ipRangeEnd,
            String gateway,
            String netmask,
            boolean isNew) throws BaseException {
//        log.info(String.format("updateIpSegment info -- ipSegmentId: %d, name: %s, hostUuid: %s, type: %s, bridge: %s, ipRangeStart: %s, ipRangeEnd: %s, gateway: %s, netmask: %s, isNew: %s",
//                ipSegmentId, name, hostUuid, type, bridge, ipRangeStart, ipRangeEnd, gateway, netmask, isNew));
//        log.info(String.format("-> invoke DB -- hostQueryByUuid, hostUuid: %s", hostUuid));
//        Host host = tableStorage.hostQueryByUuid(hostUuid);
//        log.info("<- invoke DB -- done");
//        if (host == null) {
//            log.error("ERROR: can't find host");
//            throw new BaseException("can't find host");
//        }

        IpSegment ipSegment;
        if (isNew) {
            ipSegment = new IpSegment();
        } else {
            log.info(String.format("-> invoke DB -- ipSegmentQueryById, ipSegmentId: %d", ipSegmentId));
            ipSegment = tableStorage.ipSegmentQueryById(ipSegmentId);
            log.info("<- invoke DB -- done");
        }

//        return networkProcess.saveIpSegment(ipSegment, name, hostUuid, type, bridge, ipRangeStart, ipRangeEnd, gateway, netmask);
        return networkProcess.saveIpSegment(ipSegment, name, type, ipRangeStart, ipRangeEnd, gateway, netmask);
    }

    /**
     * 删除
     */
    public String deleteIpSegment(Integer ipSegmentId){
        log.info(String.format("deleteIpSegment info -- ipSegmentId: %d", ipSegmentId));
        log.info(String.format("-> invoke DB -- ipSegmentDelete, ipSegmentId: %d", ipSegmentId));
        tableStorage.ipSegmentDelete(ipSegmentId);
        log.info("<- invoke DB -- done");
        return ResponseMsgConstants.SUCCESS;
    }

    /**
     * 网段基本详情
     */
    public IpSegment queryById(Integer ipSegmentId) {
        log.info(String.format("queryById info -- ipSegmentId: %d", ipSegmentId));
        log.info(String.format("-> invoke DB -- ipSegmentQueryById, ipSegmentId: %d", ipSegmentId));
        IpSegment ipSegment = tableStorage.ipSegmentQueryById(ipSegmentId);
        log.info("<- invoke DB -- done");
        return ipSegment;
    }

    /**
     * 段内IP分页详情
     */
    public PageResponse<IpUsed> pageQueryById(Integer ipSegmentId, Integer pageNum, Integer pageSize) {
        log.info(String.format("pageQueryById info -- ipSegmentId: %d, pageNum: %d, pageSize: %d", ipSegmentId, pageNum, pageSize));
        // get ipSegment by ipSegment
        log.info(String.format("-> invoke DB -- ipSegmentQueryById, ipSegmentId: %d", ipSegmentId));
        IpSegment ipSegment = tableStorage.ipSegmentQueryById(ipSegmentId);
        log.info("<- invoke DB -- done");

        // set ips
        List<IpUsed> ipList = new ArrayList<>();
        int ipBegin = IpUtils.stringToInt(ipSegment.getIpRangeStart());
        int ipEnd = IpUtils.stringToInt(ipSegment.getIpRangeEnd());
        Map<String,IpUsed> ips = new HashMap<>();
        log.info(String.format("-> invoke DB -- ipUsedQueryAllByIpSegmentId, ipSegmentId: %d", ipSegmentId));
        List<IpUsed> ipUsedList = tableStorage.ipUsedQueryAllByIpSegmentId(ipSegmentId);
        log.info("<- invoke DB -- done");
        ipUsedList.forEach(t -> ips.put(t.getIp(), t));

        for (int ip = ipBegin; ip <= ipEnd; ip++) {
            String ipStr = IpUtils.intToString(ip);
            IpUsed tempIpUsed;
            if (ips.containsKey(ipStr)) {
                tempIpUsed = ips.get(ipStr);
                String tempInstanceUuid = tempIpUsed.getInstanceUuid();
                log.info(String.format("-> invoke DB -- vmQueryByUuid, tempInstanceUuid: %s", tempInstanceUuid));
                Vm temp = tableStorage.vmQueryByUuid(tempInstanceUuid);
                log.info("<- invoke DB -- done");
                tempIpUsed.setInstanceName(temp.getName());
            } else {
                tempIpUsed = new IpUsed();
                tempIpUsed.setIp(ipStr);
            }
            ipList.add(tempIpUsed);
        }

        // page ipList
        List<IpUsed> listSort = new ArrayList<>();
        Integer totalElements = ipList.size();
        int totalPages = (totalElements % pageSize == 0) ? (totalElements / pageSize) : (totalElements / pageSize) + 1;
        int pageStart = pageNum == 1 ? 0 : (pageNum - 1) * pageSize;
        int pageEnd = Math.min(totalElements, pageNum * pageSize);
        if (totalElements > pageStart) {
            listSort =ipList.subList(pageStart, pageEnd);
        }

        return new PageResponse<>(listSort, totalPages, totalElements.longValue());
    }
}
