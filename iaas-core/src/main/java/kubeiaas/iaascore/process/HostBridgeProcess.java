package kubeiaas.iaascore.process;

import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.HostBridge;
import kubeiaas.common.bean.IpSegment;
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
public class HostBridgeProcess {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private DhcpScheduler dhcpScheduler;

    /**
     * save IpSegment
     */
    public HostBridge saveHostBridge(
            HostBridge hostBridge,
//            String hostUuid,
            String bridgeName,
            Integer ipSegmentId,
            String hostName) {
        Host host = tableStorage.hostQueryByName(hostName);
        String hostUuid = host.getUuid();
        // 1. update DB
        hostBridge.setHostUuid(hostUuid);
        hostBridge.setBridgeName(bridgeName);
        hostBridge.setIpSegmentId(ipSegmentId);
        hostBridge.setHostName(hostName);

        log.info("-> invoke DB -- hostBridgeSave");
        hostBridge = tableStorage.hostBridgeSave(hostBridge);
        log.info("<- invoke DB -- done");

        // 2. update DHCP config
//        log.info("-> invoke dhcpScheduler -- updateIpSeg");
//        dhcpScheduler.updateIpSeg(String.valueOf(hostBridge.getIpSegmentId()));
//        log.info("<- invoke dhcpScheduler -- done");

        return hostBridge;
    }


}
