package kubeiaas.iaascore.service;

import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.IpSegment;
import kubeiaas.common.constants.bean.IpSegmentConstants;
import kubeiaas.common.enums.network.IpTypeEnum;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.process.NetworkProcess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
}
