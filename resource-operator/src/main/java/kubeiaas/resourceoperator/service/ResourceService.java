package kubeiaas.resourceoperator.service;

import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.HostSelectStrategyConstants;
import kubeiaas.resourceoperator.dao.TableStorage;
import kubeiaas.resourceoperator.process.HostSelectProcess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class ResourceService {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private HostSelectProcess hostSelectProcess;

    public Host selectHostByAppoint(String vmUuid, String hostUuid) {
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        Host host = tableStorage.hostQueryByUuid(hostUuid);

        if (host == null) {
            log.info("host 不存在");
            return null;
        }
        if (!hostSelectProcess.checkHostAvailable(host, vm)) {
            log.info("host 资源不足");
            return null;
        }
        log.info("host 可用");
        return host;
    }

    public Host selectHostByStrategy(String vmUuid, String strategy) {
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);

        Host resultHost = new Host();
        if (strategy.equals(HostSelectStrategyConstants.ROUND_ROBIN)) {
            // 1. RoundRobin
            resultHost = hostSelectProcess.RoundRobin(vm);
        } else {
            // 0. fall in Default RoundRobin
            resultHost = hostSelectProcess.RoundRobin(vm);
        }

        return resultHost;
    }

}
