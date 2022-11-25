package kubeiaas.iaasagent.service;

import kubeiaas.common.bean.*;
import kubeiaas.common.constants.bean.VmConstants;
import kubeiaas.common.enums.image.ImageOSTypeEnum;
import kubeiaas.common.enums.network.IpTypeEnum;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.utils.*;
import kubeiaas.iaasagent.config.LibvirtConfig;
import kubeiaas.iaasagent.config.XmlConfig;
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

    @Resource
    private XmlConfig xmlConfig;

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
                instance.setStatus(VmStatusEnum.ERROR);
                tableStorage.vmSave(instance);
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
                    instance.setStatus(VmStatusEnum.ERROR);
                    tableStorage.vmSave(instance);
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
            tableStorage.vmSave(instance);
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
        Domain domain = null;
        try {
            domain = getDomainByUuid(vmUuid);
            destroyDomain(domain);
            domain.undefine();
//            vncService.deleteVncToken(vmUuid);
            log.info("deleteVm ---- end ---- Delete Domain Successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            if (domain == null) {
                log.error("deleteVm ---- end ---- Domain NotFound!");
                return true;
            } else {
                log.error("deleteVm ---- end ---- Delete Domain Error!");
                return false;
            }
        }
        return true;
    }

    /**
     * 修改虚拟机cpu和memory
     * @param VmUuid
     */
    public Boolean modifyVm(String VmUuid, int cpu, int memory) {
        log.info("modifyVm ---- start ---- VmUuid: " + VmUuid );
        Vm instance = tableStorage.vmQueryByUuid(VmUuid);
        try {
            long memories = VmCUtils.memUnitConvert(memory);
            Domain domain = virtCon.domainLookupByUUIDString(VmUuid);
            if (instance.getStatus().equals(VmStatusEnum.ACTIVE)) {
                if (cpu != 0) {
                    domain.setVcpus(cpu);
                    log.info("modifyVm -- domain set cpu: " + cpu);
                }
                if (memory != 0) {
                    domain.setMemory(memories);
                    log.info("modifyVm -- domain set memory: " + memory);
                }
            }
            String xmlDesc = domain.getXMLDesc(0);
            String xml  = xmlConfig.modifyXml(xmlDesc, cpu, memory);
            Domain d = virtCon.domainDefineXML(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        log.info("modifyVm ---- end ----");
        return true;
    }


    /**
     * 停止虚拟机
     * @param VmUuid
     */
    public boolean stopVm(String VmUuid) {
        log.info("stopVm ---- start ---- VmUuid: " + VmUuid);
        try {
            Domain d = getDomainByUuid(VmUuid);
            log.info("shutdownDomain ---- start ----");
            try {
                if (d.isActive() > 0) {
                    d.shutdown();
                } else {
                    log.error("shutdownDomain -- The domain is already dead. Shutdown failed!");
                }
                new Thread(() -> {
                    try {
                        int waitLoop = 20;
                        while (d.isActive() > 0 && waitLoop > 0) {
                            waitLoop--;
                            log.debug("shutdownDomain -- waitLoop = " + waitLoop);
                            TimeUnit.SECONDS.sleep(1);
                        }
                        if (d.isActive() > 0) {
                            log.info("shutdownDomain -- after 20 loops ,the vm is still alive, call destory funs");
                            destroyDomain(d);
                        }
                        log.debug("shutdownDomain -- d.getInfo().state = " + d.getInfo().state);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (Exception e) {
                setVmStatus(VmUuid, VmStatusEnum.ACTIVE);
                e.printStackTrace();
                return false;
            }
            log.info("shutdownDomain ---- end ----");

            // status
            Vm vm = tableStorage.vmQueryByUuid(VmUuid);
            vm.setStatus(VmStatusEnum.STOPPED);

            // save
            tableStorage.vmSave(vm);

            log.info("stopVm ---- end ---- Shutdown Domain Successfully.");
        } catch (Exception e) {
            log.info("stopVm ---- end ---- Shutdown Domain Error!");
            setVmStatus(VmUuid, VmStatusEnum.ERROR);
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * 启动虚拟机
     * @param VmUuid
     */
    public Boolean startVm(String VmUuid) {
        log.info("startVm ---- start ---- VmUuid: " + VmUuid);
        try {
            Domain d = getDomainByUuid(VmUuid);
            log.info("startDomain ---- start ----");
            try {
                if (d.isActive() == 0) {
                    d.create();
                } else {
                    log.info("startDomain -- Domain is already running!");
                }
                new Thread(() -> {
                    try {
                        int waitLoop = 3;
                        while (d.isActive() == 0 && waitLoop > 0) {
                            waitLoop--;
                            Thread.sleep(1000);
                        }
                        if (d.isActive() == 0) {
                            throw new Exception("start domain error!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (Exception e) {
                setVmStatus(VmUuid, VmStatusEnum.STOPPED);
                e.printStackTrace();
                return false;
            }

            if (d.isActive() == 1) {
                log.info("startDomain ---- end ---- Domain Started");
            } else {
                log.info("startDomain ---- end ---- Failed to start Domain in 20 secs!!");
            }

            // status
            Vm vm = tableStorage.vmQueryByUuid(VmUuid);
            vm.setStatus(VmStatusEnum.ACTIVE);

            // new vnc port
            String vncPort = ShellUtils.getCmd(LibvirtConfig.getVncPort + " " + d.getUUIDString()).replaceAll("\\r\\n|\\r|\\n|\\n\\r|:", "");          //获取新建虚拟机的VncPort
            vm.setVncPort(vncPort);

            // save
            tableStorage.vmSave(vm);
        } catch (Exception e) {
            setVmStatus(VmUuid, VmStatusEnum.ERROR);
            e.printStackTrace();
            return false;
        }
        log.info("startVm ---- end ----");
        return true;
    }


    /**
     * 重启虚拟机
     * @param VmUuid
     * @return
     */
    public Boolean rebootVm(String VmUuid) {
        log.info("rebootVm ---- start ---- VmUuid: " + VmUuid);
        try {
            Domain domain = getDomainByUuid(VmUuid);
            if (domain.isActive() == 0) {
                try {
                    domain.create(0);
                    String vncPort = ShellUtils.getCmd(LibvirtConfig.getVncPort + " " + domain.getUUIDString()).replaceAll("\\r\\n|\\r|\\n|\\n\\r|:", "");      //获取新建虚拟机的VncPort
                    Vm vm = tableStorage.vmQueryByUuid(VmUuid);
                    vm.setVncPort(vncPort);
                    vm.setStatus(VmStatusEnum.ACTIVE);
                    tableStorage.vmSave(vm);
                } catch (Exception e) {
                    log.error("rebootVm ---- end ---- Domain Starting failed!");
                    setVmStatus(VmUuid, VmStatusEnum.STOPPED);
                    e.printStackTrace();
                    return false;
                }
            } else {
                try {
                    domain.reboot(0);
                    String vncPort = ShellUtils.getCmd(LibvirtConfig.getVncPort + " " + domain.getUUIDString()).replaceAll("\\r\\n|\\r|\\n|\\n\\r|:", "");      //获取新建虚拟机的VncPort
                    Vm vm = tableStorage.vmQueryByUuid(VmUuid);
                    vm.setVncPort(vncPort);
                    vm.setStatus(VmStatusEnum.ACTIVE);
                    tableStorage.vmSave(vm);
                } catch (Exception e) {
                    log.error("rebootVm ---- end ---- Domain Starting failed!");
                    setVmStatus(VmUuid, VmStatusEnum.ACTIVE);
                    e.printStackTrace();
                    return false;
                }
            }
            log.info("rebootVm ---- end ---- Domain is rebooting.");
        } catch (Exception e) {
            log.error("rebootVm ---- end ---- Domain Starting failed!");
            setVmStatus(VmUuid, VmStatusEnum.ERROR);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 挂起虚拟机
     * @param VmUuid
     * @return
     */
    public Boolean suspendVm(String VmUuid) {
        log.info("suspendVm ---- start ---- VmUuid: " + VmUuid);
        try {
            Domain domain = getDomainByUuid(VmUuid);
            if (domain.isActive() > 0) {
                try {
                    domain.managedSave();
                    setVmStatus(VmUuid, VmStatusEnum.SUSPENDED);
                } catch (Exception e) {
                    log.error("suspendVm ---- end ---- Domain suspend failed!");
                    setVmStatus(VmUuid, VmStatusEnum.ACTIVE);
                    e.printStackTrace();
                    return false;
                }
            } else {
                log.error("suspendDomain -- The domain is already dead. suspend failed!");
                setVmStatus(VmUuid, VmStatusEnum.STOPPED);
                return false;
            }
            log.info("suspendVm ---- end ---- Domain is suspending.");
        } catch (Exception e) {
            log.error("suspendVm ---- end ---- Domain Starting failed!");
            setVmStatus(VmUuid, VmStatusEnum.ERROR);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 恢复虚拟机
     * @param VmUuid
     * @return
     */
    public Boolean resumeVm(String VmUuid) {
        log.info("resumeVm ---- start ---- VmUuid: " + VmUuid);
        try {
            Domain domain = getDomainByUuid(VmUuid);
            if (domain.isActive() > 0){
                log.error("resumeVm ---- end ---- Domain resume failed! ---- domain is still active");
                setVmStatus(VmUuid, VmStatusEnum.ACTIVE);
                return false;
            }
            domain.create(0);
            Vm vm = tableStorage.vmQueryByUuid(VmUuid);
            String vncPort = ShellUtils.getCmd(LibvirtConfig.getVncPort + " " + domain.getUUIDString()).replaceAll("\\r\\n|\\r|\\n|\\n\\r|:", "");      //获取新建虚拟机的VncPort
            vm.setStatus(VmStatusEnum.ACTIVE);
            vm.setVncPort(vncPort);
            tableStorage.vmSave(vm);
            log.info("resumeVm ---- end ---- Domain is resuming.");
        } catch (Exception e) {
            log.error("resumeVm ---- end ---- Domain resume failed! ---- domain is dead");
            setVmStatus(VmUuid, VmStatusEnum.STOPPED);
            log.error("resumeVm ---- end ---- Domain resume failed!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // ===== 已废弃 =====
    // 暂停虚拟机（已废弃）
    /*
    public Boolean suspendVm(String VmUuid) {
        log.info("suspendVm ---- start ---- VmUuid: " + VmUuid);
        try {
            Domain domain = getDomainByUuid(VmUuid);
            if (domain.isActive() > 0) {
                try {
                    domain.suspend();
                    setVmStatus(VmUuid, VmStatusEnum.SUSPENDED);
                } catch (Exception e) {
                    log.error("suspendVm ---- end ---- Domain suspend failed!");
                    setVmStatus(VmUuid, VmStatusEnum.ACTIVE);
                    e.printStackTrace();
                    return false;
                }
            } else {
                log.error("suspendDomain -- The domain is already dead. suspend failed!");
                setVmStatus(VmUuid, VmStatusEnum.STOPPED);
                return false;
            }
            log.info("suspendVm ---- end ---- Domain is suspending.");
        } catch (Exception e) {
            log.error("suspendVm ---- end ---- Domain Starting failed!");
            setVmStatus(VmUuid, VmStatusEnum.ERROR);
            e.printStackTrace();
            return false;
        }
        return true;
    }
     */

    // ===== 已废弃 =====
    // 恢复（暂停的虚拟机）
    /*
    public Boolean resumeVm(String VmUuid) {
        log.info("resumeVm ---- start ---- VmUuid: " + VmUuid);
        try {
            Domain domain = getDomainByUuid(VmUuid);
            if (domain.isActive() > 0){
                log.error("resumeVm ---- end ---- Domain resume failed! ---- domain is still active");
                setVmStatus(VmUuid, VmStatusEnum.ACTIVE);
                return false;
            }
            domain.resume();
            Vm vm = tableStorage.vmQueryByUuid(VmUuid);
            String vncPort = ShellUtils.getCmd(LibvirtConfig.getVncPort + " " + domain.getUUIDString()).replaceAll("\\r\\n|\\r|\\n|\\n\\r|:", "");      //获取新建虚拟机的VncPort
            vm.setStatus(VmStatusEnum.ACTIVE);
            vm.setVncPort(vncPort);
            tableStorage.vmSave(vm);
            log.info("resumeVm ---- end ---- Domain is resuming.");
        } catch (Exception e) {
            log.error("resumeVm ---- end ---- Domain resume failed! ---- domain is dead");
            setVmStatus(VmUuid, VmStatusEnum.STOPPED);
            log.error("resumeVm ---- end ---- Domain resume failed!");
            e.printStackTrace();
            return false;
        }
        return true;
    }
     */

    /**
     * 强制关闭虚拟机
     * @param d
     * @throws Exception
     */
    private void destroyDomain(Domain d) throws Exception {
        log.info("destroyDomain ---- start ----");

        if (d.isActive() > 0) {
            d.destroy();
        }
        new Thread(() -> {
            try {
                int waitLoop = 3;
                while (d.isActive() > 0 && waitLoop > 0) {
                    waitLoop--;
                    Thread.sleep(1000);
                }
                if (d.isActive() > 0) {
                    throw new Exception("destroyDomain -- destroy error!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        log.info("destroyDomain ---- end ----");
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

    private void setVmStatus(String VmUuid, VmStatusEnum status){

        Vm vm = tableStorage.vmQueryByUuid(VmUuid);
        vm.setStatus(status);
        tableStorage.vmSave(vm);
    }

}
