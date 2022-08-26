package kubeiaas.iaascore.scheduler;

import kubeiaas.common.bean.IpUsed;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.enums.network.IpAttachEnum;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.dao.feign.DhcpController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Slf4j
@Configuration
public class DhcpScheduler {
    @Resource
    private DhcpController dhcpController;

    @Resource
    private TableStorage tableStorage;

    public Boolean bindMacAndIp(IpUsed ipUsed) {
        if (dhcpController.bindMacAndIp(ipUsed.getInstanceUuid(), ipUsed.getMac(), ipUsed.getIp())
                .equals(ResponseMsgConstants.SUCCESS)) {
            ipUsed.setStatus(IpAttachEnum.ATTACHED);
            tableStorage.ipUsedSave(ipUsed);
            return true;
        } else {
            return false;
        }
    }

}
