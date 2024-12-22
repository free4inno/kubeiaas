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
        log.info("selectHostByAppoint -- vmUuid: {}, hostUuid: {}", vmUuid, hostUuid);
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        Host host = tableStorage.hostQueryByUuid(hostUuid);

        if (host == null) {
            log.error("ERROR: host is null, could not find by this uuid");
            return null;
        }
        if (!hostSelectProcess.checkHostAvailable(host, vm)) {
            log.warn("WARN: host not available, return null");
            return null;
        }
        log.info("success -- host available");
        return host;
    }

    public Host selectHostByHostUuid(String hostUuid) {
        log.info("selectHostByHostUuid -- hostUuid: {}", hostUuid);
        Host host = tableStorage.hostQueryByUuid(hostUuid);

        if (host == null) {
            log.error("ERROR: host is null, could not find by this uuid");
            return null;
        }
        log.info("success -- host available");
        return host;
    }

    public Host selectHostByStrategy(String vmUuid, String strategy) {
        log.info("selectHostByStrategy -- vmUuid: {}, strategy: {}", vmUuid, strategy);
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

    public Host selectHostByStrategy(String strategy) {
        log.info("selectHostByStrategy -- strategy: {}", strategy);
        Host resultHost = new Host();
        if (strategy.equals(HostSelectStrategyConstants.ROUND_ROBIN)) {
            // 1. RoundRobin
            resultHost = hostSelectProcess.RoundRobin();
        } else {
            // 0. fall in Default RoundRobin
            resultHost = hostSelectProcess.RoundRobin();
        }
        return resultHost;
    }

}
