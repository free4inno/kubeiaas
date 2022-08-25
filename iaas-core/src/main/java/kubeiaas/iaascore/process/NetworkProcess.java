package kubeiaas.iaascore.process;

import kubeiaas.common.bean.IpSegment;
import kubeiaas.common.bean.IpUsed;
import kubeiaas.common.constants.bean.IpUsedConstants;
import kubeiaas.common.utils.IpUtils;
import kubeiaas.common.utils.MacUtils;
import kubeiaas.iaascore.dao.TableStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NetworkProcess {

    @Resource
    private TableStorage tableStorage;

    public String getNewMac(int ipSegmentId) {
        if (ipSegmentId <= 0 || ipSegmentId >= 4096) {
            // when id <= 0, illegal;
            // when id >= 4096, up to top.
            return IpUsedConstants.DEFAULT_MAC;
        } else {
            IpSegment ipSegment = tableStorage.ipSegmentQueryById(ipSegmentId);
            if (ipSegment == null) {
                return IpUsedConstants.DEFAULT_MAC;
            }
            String macPre = MacUtils.getMacPre(ipSegmentId, ipSegment.getType());
            return MacUtils.getMACAddress(macPre);
        }
    }

    public IpUsed getNewIp(int ipSegmentId) {
        IpSegment ipSegment = tableStorage.ipSegmentQueryById(ipSegmentId);
        if (ipSegment == null) {
            return null;
        }
        return IpUtils.getIpAddress(getAllUsedIp(ipSegmentId), ipSegment);
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
}
