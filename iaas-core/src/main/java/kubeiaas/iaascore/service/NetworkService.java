package kubeiaas.iaascore.service;

import kubeiaas.common.bean.*;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.IpSegmentConstants;
import kubeiaas.common.enums.network.IpTypeEnum;
import kubeiaas.common.utils.IpUtils;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.process.NetworkProcess;
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

    public IpSegment createIpSegment(
            String name,
            String hostName,
            String type,
            String bridge,
            String ipRangeStart,
            String ipRangeEnd,
            String gateway,
            String netmask) throws BaseException {
        // 建立 name -> uuid 的索引
        Map<String, String> hostNameUuidMap = new HashMap<>();
        List<Host> hostList = tableStorage.hostQueryAll();
        for (Host host : hostList) {
            hostNameUuidMap.put(host.getName(), host.getUuid());
        }

        String hostUuid = hostNameUuidMap.get(hostName);
        if (hostUuid == null){
            throw new BaseException("can't find host");
        }

        IpSegment newIpSegment = networkProcess.createIpSegment(name, hostUuid, type, bridge, ipRangeStart, ipRangeEnd, gateway, netmask);
        return newIpSegment;
    }

    public String deleteIpSegment(Integer ipSegmentId){
        tableStorage.ipSegmentDelete(ipSegmentId);
        return ResponseMsgConstants.SUCCESS;
    }

    public IpSegment queryById(Integer ipSegmentId) {
        IpSegment ipSegment = tableStorage.ipSegmentQueryById(ipSegmentId);

        //set ips
        List<IpUsed> ipList = new ArrayList<>();
        int ipBegin = IpUtils.stringToInt(ipSegment.getIpRangeStart());
        int ipEnd = IpUtils.stringToInt(ipSegment.getIpRangeEnd());
        Map<String,IpUsed> ips = new HashMap<>();
        List<IpUsed> ipUsedList = tableStorage.ipUsedQueryAllByIpSegmentId(ipSegmentId);
        ipUsedList.forEach(t->{
            ips.put(t.getIp(),t);
            });

        for (int ip = ipBegin; ip <= ipEnd; ip++) {
            String ipStr = IpUtils.intToString(ip);
            if (ips.containsKey(ipStr)){
                IpUsed tempIpUsed = new IpUsed();
                tempIpUsed = ips.get(ipStr);
                String tempInstanceUuid = tempIpUsed.getInstanceUuid();
                tempIpUsed.setInstanceName(tableStorage.vmQueryByUuid(tempInstanceUuid).getName());
                ipList.add(tempIpUsed);
            }else {
                IpUsed tempIpUsed = new IpUsed();
                tempIpUsed.setIp(ipStr);
                ipList.add(tempIpUsed);
            }
            }

        ipSegment.setIps(ipList);

        return ipSegment;
    }

    public IpSegment pageQueryById(Integer ipSegmentId, Integer pageNum, Integer pageSize) {

        //get ipSegment by ipSegment
        IpSegment ipSegment = tableStorage.ipSegmentQueryById(ipSegmentId);

        //set ips
        List<IpUsed> ipList = new ArrayList<>();
        int ipBegin = IpUtils.stringToInt(ipSegment.getIpRangeStart());
        int ipEnd = IpUtils.stringToInt(ipSegment.getIpRangeEnd());
        Map<String,IpUsed> ips = new HashMap<>();
        List<IpUsed> ipUsedList = tableStorage.ipUsedQueryAllByIpSegmentId(ipSegmentId);
        ipUsedList.forEach(t->{
            ips.put(t.getIp(),t);
            });
        for (int ip = ipBegin; ip <= ipEnd; ip++) {
            String ipStr = IpUtils.intToString(ip);
            if (ips.containsKey(ipStr)){
                IpUsed tempIpUsed = new IpUsed();
                tempIpUsed = ips.get(ipStr);
                String tempInstanceUuid = tempIpUsed.getInstanceUuid();
                tempIpUsed.setInstanceName(tableStorage.vmQueryByUuid(tempInstanceUuid).getName());
                ipList.add(tempIpUsed);
            }else {
                IpUsed tempIpUsed = new IpUsed();
                tempIpUsed.setIp(ipStr);
                ipList.add(tempIpUsed);
            }
            }
        //page ipList
        List<IpUsed> listSort = new ArrayList<>();
        int size = ipList.size();
        int pageStart=pageNum==1?0:(pageNum-1)*pageSize;
        int pageEnd=size<pageNum*pageSize?size:pageNum*pageSize;;
        if(size>pageStart){
            listSort =ipList.subList(pageStart, pageEnd);
        }
        ipSegment.setIps(listSort);
        return ipSegment;
    }

    public IpSegment editIpSegment(
            Integer ipSegmentId,
            String name,
            String hostName,
            String type,
            String bridge,
            String ipRangeStart,
            String ipRangeEnd,
            String gateway,
            String netmask) throws BaseException {

        IpSegment ipSegment = tableStorage.ipSegmentQueryById(ipSegmentId);
        // 建立 name -> uuid 的索引
        Map<String, String> hostNameUuidMap = new HashMap<>();
        List<Host> hostList = tableStorage.hostQueryAll();
        for (Host host : hostList) {
            hostNameUuidMap.put(host.getName(), host.getUuid());
        }

        String hostUuid = hostNameUuidMap.get(hostName);
        if (hostUuid == null){
            throw new BaseException("can't find host");
        }

        IpSegment newIpSegment = networkProcess.editIpSegment(ipSegment,name, hostUuid, type, bridge, ipRangeStart, ipRangeEnd, gateway, netmask);
        return newIpSegment;
    }

}
