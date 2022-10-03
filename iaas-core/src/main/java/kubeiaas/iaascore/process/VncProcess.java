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
        log.info("createVm -- setting VNC start");
        // 1. GET VM UUID
        String vmUuid = newVm.getUuid();
        // 异步执行 vmCreate 需要重新查库获取 vnc port & password
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);

        // 2. GET VNC PORT
        String vncPort = vm.getVncPort();

        // 3. GET HOST IP
        Host host = tableStorage.hostQueryByUuid(vm.getHostUuid());
        String vncIp = host.getIp();

        // 4. BUILD ADDRESS AND ADD
        String address = vncIp + ":" + (Integer.parseInt(vncPort) + 5900);
        vncScheduler.addVncToken(vmUuid, address);
        log.info("createVm -- setting VNC end");
    }

    public void deleteVncToken(String vmUuid){
        log.info("deleteVm --  delete VNC start");
        vncScheduler.deleteVncToken(vmUuid);
        log.info("deleteVm --  delete VNC end");
    }
}

