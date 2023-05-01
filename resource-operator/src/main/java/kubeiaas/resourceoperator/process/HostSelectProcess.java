package kubeiaas.resourceoperator.process;

import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.Vm;
import kubeiaas.resourceoperator.dao.TableStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class HostSelectProcess {

    @Resource
    private TableStorage tableStorage;

    private static int roundRobinCount = 0;


    // 选择宿主机算法
    public Host RoundRobin() {
        List<Host> hostList = tableStorage.hostQueryAll();
        if (hostList == null || hostList.isEmpty()) {
            return null;
        }

        Host resultHost = new Host();
        for (int checkCount = 0; checkCount < hostList.size(); checkCount++) {
            roundRobinCount += 1;
            if (roundRobinCount >= hostList.size()) {
                roundRobinCount = 0;
            }
            resultHost = hostList.get(roundRobinCount);
            return resultHost;
        }
        return null;
    }

    // 选择宿主机算法
    public Host RoundRobin(Vm vm) {
        List<Host> hostList = tableStorage.hostQueryAll();
        if (hostList == null || hostList.isEmpty()) {
            return null;
        }

        Host resultHost = new Host();
        for (int checkCount = 0; checkCount < hostList.size(); checkCount++) {
            roundRobinCount += 1;
            if (roundRobinCount >= hostList.size()) {
                roundRobinCount = 0;
            }
            resultHost = hostList.get(roundRobinCount);
            if (checkHostAvailable(resultHost, vm)) {
                return resultHost;
            }
        }
        return null;
    }

    public Boolean checkHostAvailable(Host host, Vm vm) {
        // todo: 根据所需资源情况检查 host 是否满足 vm 可用
        return true;
    }
}
