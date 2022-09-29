package kubeiaas.iaascore.process;

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

    public void addVncToken(Vm newvm){

       log.info("createVm -- setting VNC");
       String vmUuid = newvm.getUuid();
       Vm vm = tableStorage.vmQueryByUuid(vmUuid);
       String vncPort = vm.getVncPort();
       log.info("portGot: " + vncPort);
       String dhcpHostIp = System.getenv("DHCP_HOST_IP");
       String address =dhcpHostIp+ ":" + (Integer.parseInt(vncPort) + 5900);
       vncScheduler.addVncToken(vmUuid, address);
       
    }

    public void deleteVncToken(String vmUuid){
        log.info("deleteVm --  delete VNC start");
        vncScheduler.deleteVncToken(vmUuid);
        log.info("deleteVm --  delete VNC end");
    }
}

