package kubeiaas.iaasagent.service;

import kubeiaas.common.bean.*;
import kubeiaas.common.constants.bean.VmConstants;
import kubeiaas.common.enums.image.ImageOSTypeEnum;
import kubeiaas.common.enums.network.IpTypeEnum;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.utils.*;
import kubeiaas.iaasagent.config.LibvirtConfig;
import kubeiaas.iaasagent.dao.TableStorage;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VmService {

    private static Connect virtCon;

    @Resource
    private TableStorage tableStorage;

    @Resource
    private VncService vncService;

    @Resource
    private LibvirtConfig libvirtConfig;

    public VmService() {
        if (virtCon == null) {
            String conStr = LibvirtConfig.virConStr;
            try {
                virtCon = new Connect(conStr);
            } catch (LibvirtException e) {
                log.error("get virt connection error", e);
                log.error(e.getMessage());
            }
        }
    }

    public boolean createVm(String vmUuid) {
        log.info("createVm ---- start ---- instanceUuid: " + vmUuid);

        Vm instance = tableStorage.vmQueryByUuid(vmUuid);
        Image image = tableStorage.imageQueryByUuid(instance.getImageUuid());
        Host host = tableStorage.hostQueryByUuid(instance.getHostUuid());
        List<Volume> volumes = tableStorage.volumeQueryAllByInstanceUuid(vmUuid);
        List<IpUsed> ips = tableStorage.ipUsedQueryAllByInstanceUuid(vmUuid);

        log.info("createVm -- 0. init LibvirtConfig");

        String xml = libvirtConfig.generateDesXML(instance, image, volumes, ips, instance.getCpus(), instance.getMemory());
        log.info("createVm -- xml generated: " + xml);

        // TODO: windows 解挂
        /*
        List<Volume> tempVolumes = tableStorage.getIsoVolumesByInstanceUuid(appKey, instance.getUuid());
        for (Volume volume : tempVolumes) {
            tableStorage.deleteVolumeByUuid(appKey, volume.getVolumeUuid()); //for wins' 镜像+驱动
        }
        */

        // new a libvirt domain
        Domain domain;

        // detach active
        try {
            domain = virtCon.domainLookupByUUIDString(instance.getUuid());
            if (domain.isActive() != 0) {
                return false;                //solve openfeign retry strategy;
            }
        } catch (LibvirtException e) {
            log.info("createVm -- 1. start creating Domain");
        }

        // confirm to run
        try {
            // Step 1：创建+启动虚拟机 -----------------------------------------
            log.info("domain xml: " + xml);
            Domain d = virtCon.domainDefineXML(xml);    //define and start vm
            domain = virtCon.domainLookupByUUIDString(instance.getUuid());
            Thread.sleep(500);
            if (domain.isActive() == 0) {
                int rs = d.create();
                Thread.sleep(500);
                if (rs == 0) {
                    log.info("create domain successful!");
                } else {
                    log.info("create domain failed!");
                    return false;
                }
            }
            // Step 2：配置vnc服务 ---------------------------------------------
            log.info("createVm -- 2. presetting setting VNC");
            String vncPort = ShellUtils.getCmd(LibvirtConfig.getVncPort + " " + domain.getUUIDString()).replaceAll("\\r\\n|\\r|\\n|\\n\\r|:", "");          //获取新建虚拟机的VncPort；这个字符串操作是拿到了上面xml里配置的domain的uuid
            log.info("portSet: " + vncPort);
            String vncPasswd = VmCUtils.getVNCPasswd(instance.getId(), instance.getUuid());    //获取新建虚拟机的密码

//            vncService.addVncToken(instance.getUuid(), host.getIp() + ":" + (Integer.parseInt(vncPort) + 5900));

            instance.setVncPort(vncPort);
            instance.setVncPassword(vncPasswd);

            // Step 3：改Linux虚拟机密码（选）-------------------------------------
            String newPassword = instance.getPassword();
            String oldPassword = VmConstants.DEFAULT_PASSWORD;
            if (!oldPassword.equals(newPassword) && image.getOsType() == ImageOSTypeEnum.LINUX && newPassword != null && !newPassword.isEmpty()){
                log.info("createVm -- 3. setting vm password");
                TimeUnit.SECONDS.sleep(45);

                String privateIp = "";
                for (IpUsed ip : ips) {
                    if (ip.getType().equals(IpTypeEnum.PRIVATE)) {
                        privateIp = ip.getIp();
                    }
                }
                log.info("VM Passwd Modification: get privIp=" + privateIp);

                // TODO: 执行脚本ssh连接改密码
                // String ret1 = ShellUtils.getNohupCmd("expect /root/iaas-deploy/change_vm_pwd.sh" + " " + privIp + " " + oldPassword + " " + newPassword);

                TimeUnit.SECONDS.sleep(10);
            } else {
                log.info("createVm -- 3. default vm password");
            }
            instance.setStatus(VmStatusEnum.ACTIVE);
        } catch (Exception e) {
            e.printStackTrace();
            instance.setStatus(VmStatusEnum.ERROR);
            return false;
        } finally {
            log.info("createVm -- 4. save into DB");
            tableStorage.vmSave(instance);
        }
        log.info("createVm ---- end ----");
        return true;
    }

    public boolean deleteVm(String vmUuid){
        log.info("deleteVm ---- start ---- instanceUuid: " + vmUuid);
        try {
            Domain domain = getDomainByUuid(vmUuid);
            log.info("destroyDomain ---- start ----");
            if (domain.isActive() > 0) {
                domain.destroy();
            }
            new Thread(() -> {
                try {
                    int waitLoop = 3;
                    while (domain.isActive() > 0 && waitLoop > 0) {
                        waitLoop--;
                        Thread.sleep(1000);
                    }
                    if (domain.isActive() > 0) {
                        throw new Exception("destroyDomain -- destroy error!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            domain.undefine();

//            vncService.deleteVncToken(vmUuid);

            log.info("deleteVm ---- end ---- Delete Domain Successfully.");
        } catch (Exception e) {
            log.error("deleteVm ---- end ---- Delete Domain Error!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 修改虚拟机cpu和memory
     * @param VmUuid
     */
    public Boolean modifyVm(String VmUuid){
        log.info("modifyVm ---- start ---- VmUuid: " + VmUuid );
        Vm instance = tableStorage.vmQueryByUuid(VmUuid);
        int cpu = instance.getCpus();
        int memory = instance.getMemory();
        try {
            long memories = VmCUtils.memUnitConvert(memory);
            Domain domain = virtCon.domainLookupByUUIDString(VmUuid);
            if (cpu != 0) {
                domain.setVcpus(cpu);
                log.info("modifyVm -- domain set cpu: " + cpu);
            }
            if (memory != 0) {
                domain.setMemory(memories);
                log.info("modifyVm -- domain set memory: " + memory);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        log.info("modifyVm ---- end ----");
        return true;
    }

    private Domain getDomainByUuid(String vmUuid) throws Exception {
        log.info("getDomainByUuid ---- start ----");
        if (vmUuid == null) {
            log.info("getDomainByUuid ---- throws ---- vm uuid is empty");
            throw new Exception("vm uuid is empty");
        }

        Domain d = virtCon.domainLookupByUUIDString(vmUuid);
        if (d == null) {
            log.info("getDomainByUuid ---- throws ---- no domain with uuid: " + vmUuid);
            throw new Exception("no domain with uuid: " + vmUuid);
        }
        log.info("getDomainByUuid ---- end ----");
        return d;
    }
}
