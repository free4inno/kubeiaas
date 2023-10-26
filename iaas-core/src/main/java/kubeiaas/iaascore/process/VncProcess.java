package kubeiaas.iaascore.process;

import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.Vm;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.scheduler.VncScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class VncProcess {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private VncScheduler vncScheduler;

    public void addVncToken(Vm newVm){
        // 1. GET VM UUID
        String vmUuid = newVm.getUuid();
        // 异步执行 vmCreate 需要重新查库获取 vnc port & password
        log.info("-> invoke DB -- vmQueryByUuid");
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        log.info("<- invoke DB -- done");

        // 2. GET VNC PORT
        String vncPort = vm.getVncPort();
        if (vncPort == null || vncPort.isEmpty()) {
            return;
        }

        // 3. GET HOST IP
        log.info("-> invoke DB -- hostQueryByUuid");
        Host host = tableStorage.hostQueryByUuid(vm.getHostUuid());
        log.info("<- invoke DB -- done");
        String vncIp = host.getIp();

        // 4. BUILD ADDRESS AND ADD
        String address = vncIp + ":" + (Integer.parseInt(vncPort) + 5900);
        log.info("-> invoke vncScheduler -- addVncToken");
        vncScheduler.addVncToken(vmUuid, address);
        log.info("<- invoke vncScheduler -- done");
    }

    public void deleteVncToken(String vmUuid){
        log.info("-> invoke vncScheduler -- deleteVncToken");
        vncScheduler.deleteVncToken(vmUuid);
        log.info("<- invoke vncScheduler -- done");
    }

    public Vm flushVncToken(String vmUuid){

        // 异步执行 vmCreate 需要重新查库获取 vnc port & password
        log.info("-> invoke DB -- vmQueryByUuid");
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        log.info("<- invoke DB -- done");

        // 2. GET VNC PORT
        String vncPort = vm.getVncPort();

        // 3. GET HOST IP
        log.info("-> invoke DB -- hostQueryByUuid");
        Host host = tableStorage.hostQueryByUuid(vm.getHostUuid());
        log.info("<- invoke DB -- done");
        String vncIp = host.getIp();

        // 4. BUILD ADDRESS AND ADD
        String address = vncIp + ":" + (Integer.parseInt(vncPort) + 5900);
        log.info("-> invoke vncScheduler -- flushVncToken");
        vncScheduler.flushVncToken(vmUuid, address);
        log.info("<- invoke DB -- done");

        return vm;
    }


}

