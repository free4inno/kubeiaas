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
        List<IpSegment> ipSegmentList = tableStorage.ipSegmentQueryAll();
        // 建立 uuid -> name 的索引
        Map<String, String> hostUuidNameMap = new HashMap<>();
        List<Host> hostList = tableStorage.hostQueryAll();
        for (Host host : hostList) {
            hostUuidNameMap.put(host.getUuid(), host.getName());
        }
        // set ipSeg hostName
        for (IpSegment ipSeg : ipSegmentList) {
            String hostName = hostUuidNameMap.get(ipSeg.getHostUuid());
            ipSeg.setHostName(hostName);
        }
        return ipSegmentList;
    }

    /**
     * statistics
     */
    public Map<String, Integer> getIpCount() {
        Map<String, Integer> resMap = new HashMap<>();
        // 1. 私网IP - 总量
        Integer privateIpTotal = networkProcess.getAllTotalNum(IpTypeEnum.PRIVATE);
        resMap.put(IpSegmentConstants.PRIVATE_TOTAL, privateIpTotal);
        // 2. 私网IP - 用量
        Integer privateIpUsed = networkProcess.getAllUsedNum(IpTypeEnum.PRIVATE);
        resMap.put(IpSegmentConstants.PRIVATE_USED, privateIpUsed);
        // 3. 公网IP - 总量
        Integer publicIpTotal = networkProcess.getAllTotalNum(IpTypeEnum.PUBLIC);
        resMap.put(IpSegmentConstants.PUBLIC_TOTAL, publicIpTotal);
        // 4. 公网IP - 用量
        Integer publicIpUsed = networkProcess.getAllUsedNum(IpTypeEnum.PUBLIC);
        resMap.put(IpSegmentConstants.PUBLIC_USED, publicIpUsed);

        return resMap;
    }

    /**
     * 新建 & 编辑
     */
    public IpSegment updateIpSegment(
            Integer ipSegmentId,
            String name,
            String hostUuid,
            String type,
            String bridge,
            String ipRangeStart,
            String ipRangeEnd,
            String gateway,
            String netmask,
            boolean isNew) throws BaseException {

        if (tableStorage.hostQueryByUuid(hostUuid) == null) {
            throw new BaseException("can't find host");
        }

        IpSegment ipSegment;
        if (isNew) {
            ipSegment = new IpSegment();
        } else {
            ipSegment = tableStorage.ipSegmentQueryById(ipSegmentId);
        }

        return networkProcess.saveIpSegment(ipSegment, name, hostUuid, type, bridge, ipRangeStart, ipRangeEnd, gateway, netmask);
    }

    /**
     * 删除
     */
    public String deleteIpSegment(Integer ipSegmentId){
        tableStorage.ipSegmentDelete(ipSegmentId);
        return ResponseMsgConstants.SUCCESS;
    }

    /**
     * 网段基本详情
     */
    public IpSegment queryById(Integer ipSegmentId) {
        return tableStorage.ipSegmentQueryById(ipSegmentId);
    }

    /**
     * 段内IP分页详情
     */
    public PageResponse<IpUsed> pageQueryById(Integer ipSegmentId, Integer pageNum, Integer pageSize) {
        // get ipSegment by ipSegment
        IpSegment ipSegment = tableStorage.ipSegmentQueryById(ipSegmentId);

        // set ips
        List<IpUsed> ipList = new ArrayList<>();
        int ipBegin = IpUtils.stringToInt(ipSegment.getIpRangeStart());
        int ipEnd = IpUtils.stringToInt(ipSegment.getIpRangeEnd());
        Map<String,IpUsed> ips = new HashMap<>();
        List<IpUsed> ipUsedList = tableStorage.ipUsedQueryAllByIpSegmentId(ipSegmentId);
        ipUsedList.forEach(t -> ips.put(t.getIp(), t));

        for (int ip = ipBegin; ip <= ipEnd; ip++) {
            String ipStr = IpUtils.intToString(ip);
            IpUsed tempIpUsed;
            if (ips.containsKey(ipStr)) {
                tempIpUsed = ips.get(ipStr);
                String tempInstanceUuid = tempIpUsed.getInstanceUuid();
                tempIpUsed.setInstanceName(tableStorage.vmQueryByUuid(tempInstanceUuid).getName());
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
