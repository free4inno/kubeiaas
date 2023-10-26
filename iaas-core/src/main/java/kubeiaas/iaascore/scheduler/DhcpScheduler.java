package kubeiaas.iaascore.scheduler;

import kubeiaas.common.bean.IpUsed;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.enums.network.IpAttachEnum;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.dao.feign.DhcpController;
import kubeiaas.iaascore.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Configuration
public class DhcpScheduler {
    @Resource
    private DhcpController dhcpController;

    @Resource
    private AgentConfig agentConfig;

    @Resource
    private TableStorage tableStorage;

    public Boolean bindMacAndIp(IpUsed ipUsed) {
        try {
            if (dhcpController.bindMacAndIp(getDhcpUri(), ipUsed.getInstanceUuid(), ipUsed.getMac(), ipUsed.getIp())
                    .equals(ResponseMsgConstants.SUCCESS)) {
                ipUsed.setStatus(IpAttachEnum.ATTACHED);
                log.info("-> invoke DB -- ipUsedSave");
                tableStorage.ipUsedSave(ipUsed);
                log.info("<- invoke DB -- done");
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error(String.format("ERROR: bindMacAndIp"));
            e.printStackTrace();
            return false;
        }
    }

    public Boolean unbindMacAndIp(String vmUuid){
        try {
            return dhcpController.unbindMacAndIp(getDhcpUri(), vmUuid)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error(String.format("ERROR: unbindMacAndIp"));
            e.printStackTrace();
            return false;
        }
    }

    public Boolean updateIpSeg(String ipSegId){
        try {
            return dhcpController.updateIpSeg(getDhcpUri(), ipSegId)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error(String.format("ERROR: updateIpSeg"));
            e.printStackTrace();
            return false;
        }
    }

    private URI getDhcpUri() {
        try {
            return new URI(agentConfig.getDhcpUri());
        } catch (URISyntaxException | BaseException e) {
            log.error(String.format("ERROR: getDhcpUri"));
            e.printStackTrace();
            log.error("build URI failed!");
            return null;
        }
    }

}
