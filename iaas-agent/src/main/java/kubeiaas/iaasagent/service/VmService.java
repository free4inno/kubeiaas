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
import org.libvirt.DomainInfo;
import org.libvirt.LibvirtException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VmService {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private VncService vncService;

    @Resource
    private LibvirtConfig libvirtConfig;

    @Resource
    private XmlConfig xmlConfig;

    public boolean createVm(String vmUuid) {
        log.info(String.format("createVm info -- vmUuid: %s", vmUuid));

        Vm instance = tableStorage.vmQueryByUuid(vmUuid);
        Image image = tableStorage.imageQueryByUuid(instance.getImageUuid());
        Host host = tableStorage.hostQueryByUuid(instance.getHostUuid());
        List<Volume> volumes = tableStorage.volumeQueryAllByInstanceUuid(vmUuid);
        List<IpUsed> ips = tableStorage.ipUsedQueryAllByInstanceUuid(vmUuid);

        // 1. 初始化LibvirtConfig并检查虚拟机是否已经存在
        log.info("STEP 1: init LibvirtConfig and check if vm exists");
        String xml = libvirtConfig.generateDesXML(instance, image, volumes, ips, instance.getCpus(), instance.getMemory());
        log.debug("createVm -- xml generated: " + xml);

        // TODO: windows 解挂

        Domain domain;

        // detach active
        try {
            Connect virtCon = LibvirtConfig.getVirtCon();
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
            // Step 2：创建+启动虚拟机 -----------------------------------------
            log.info("STEP 2: create and start vm");
            Connect virtCon = LibvirtConfig.getVirtCon();
            Domain d = virtCon.domainDefineXML(xml);    //define and start vm
            domain = virtCon.domainLookupByUUIDString(instance.getUuid());
            Thread.sleep(500);
            if (domain.isActive() == 0) {
                int rs = d.create();
                Thread.sleep(500);
                if (rs == 0) {
                    log.debug("create domain successful!");
                } else {
                    log.warn("create domain failed and set status to ERROR!");
                    instance.setStatus(VmStatusEnum.ERROR);
                    log.info("-> invoke DB -- vmSave");
                    tableStorage.vmSave(instance);
                    log.info("<- invoke DB -- done");
                    return false;
                }
            }
            // Step 3：配置vnc服务 ---------------------------------------------
            log.info("STEP 3: config vnc service");
            String vncPort = ShellUtils.getCmd(LibvirtConfig.getVncPort + " " + domain.getUUIDString()).replaceAll("\\r\\n|\\r|\\n|\\n\\r|:", "");          //获取新建虚拟机的VncPort；这个字符串操作是拿到了上面xml里配置的domain的uuid
            String vncPasswd = VmCUtils.getVNCPasswd(instance.getId(), instance.getUuid());    //获取新建虚拟机的密码
            instance.setVncPort(vncPort);
            instance.setVncPassword(vncPasswd);

            // Step 3：改Linux虚拟机密码（选）-------------------------------------
            log.info("STEP 4: change vm password(only for linux)");
            String newPassword = instance.getPassword();
            String oldPassword = VmConstants.DEFAULT_PASSWORD;
            if (!oldPassword.equals(newPassword) && image.getOsType() == ImageOSTypeEnum.LINUX && newPassword != null && !newPassword.isEmpty()){
                TimeUnit.SECONDS.sleep(45);

                String privateIp = "";
                for (IpUsed ip : ips) {
                    if (ip.getType().equals(IpTypeEnum.PRIVATE)) {
                        privateIp = ip.getIp();
                    }
                }
                log.debug(String.format("VM Passwd Modification: get privateIp = %s", privateIp));

                // TODO: 执行脚本ssh连接改密码
                // String ret1 = ShellUtils.getNohupCmd("expect /root/iaas-deploy/change_vm_pwd.sh" + " " + privIp + " " + oldPassword + " " + newPassword);

                TimeUnit.SECONDS.sleep(10);
            } else {
                log.debug("createVm -- 3. default vm password");
            }
            instance.setStatus(VmStatusEnum.ACTIVE);
        } catch (Exception e) {
            log.error("ERROR: create vm error: ", e);
            instance.setStatus(VmStatusEnum.ERROR);

            log.info("-> invoke DB -- vmSave");
            tableStorage.vmSave(instance);
            log.info("<- invoke DB -- done");

            return false;
        } finally {
            log.info("-> invoke DB -- vmSave");
            tableStorage.vmSave(instance);
            log.info("<- invoke DB -- done");
        }
        return true;
    }

    public boolean createVmByXml(String vmUuid, String xmlDesc) {
        log.info("createVmFromXml ---- start ----");
        Connect virtCon = LibvirtConfig.getVirtCon();
        // check if domain is already exist
        try {
            Domain domain = virtCon.domainLookupByUUIDString(vmUuid);
            if (domain.isPersistent() > 0) {
                // domain is already exist
                return false;
            }
        } catch (Exception e) {
            log.info("createVmFromXml -- 1. start creating Domain");
        }
        // create domain
        try {
            // Only define the domain, not start it
            virtCon.domainDefineXML(xmlDesc);
        } catch (Exception e) {
            log.error("ERROR: create vm from xml error: ", e);
            return false;
        }

        // Update vm' vnc port and password
        log.info("createVmFromXml -- 2. update vnc port and password");
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        vm.setStatus(VmStatusEnum.STOPPED);
        String vncPort = ShellUtils.getCmd(LibvirtConfig.getVncPort + " " + vmUuid).replaceAll("\\r\\n|\\r|\\n|\\n\\r|:", "");          //获取新建虚拟机的VncPort
        vm.setVncPort(vncPort);
        String vncPasswd = VmCUtils.getVNCPasswd(vm.getId(), vmUuid);
        vm.setVncPassword(vncPasswd);
        tableStorage.vmSave(vm);
        return true;
    }

    public boolean deleteVm(String vmUuid){
        log.info(String.format("deleteVm info -- vmUuid: %s", vmUuid));
        Domain domain = null;
        try {
            domain = getDomainByUuid(vmUuid);
            destroyDomain(domain);
            domain.undefine();
//            vncService.deleteVncToken(vmUuid);
            log.info("deleteVm ---- end ---- Delete Domain Successfully.");
        } catch (Exception e) {
            if (domain == null) {
                log.error("ERROR: delete vm error, domain is null: ", e);
                return true;
            } else {
                log.error("ERROR: delete vm error: ", e);
                return false;
            }
        }
        return true;
    }

    /**
     * 修改虚拟机cpu和memory
     * @param vmUuid
     */
    public Boolean modifyVm(String vmUuid, int cpu, int memory) {
        log.info(String.format("modifyVm info -- vmUuid: %s, cpu: %d, memory: %d", vmUuid, cpu, memory));

        log.info("-> invoke DB -- vmQueryByUuid");
        Vm instance = tableStorage.vmQueryByUuid(vmUuid);
        log.info("<- invoke DB -- done");

        try {
            long memories = VmCUtils.memUnitConvert(memory);
            Connect virtCon = LibvirtConfig.getVirtCon();
            Domain domain = virtCon.domainLookupByUUIDString(vmUuid);
            if (instance.getStatus().equals(VmStatusEnum.ACTIVE)) {
                if (cpu != 0) {
                    domain.setVcpus(cpu);
                }
                if (memory != 0) {
                    domain.setMemory(memories);
                }
            }
            String xmlDesc = domain.getXMLDesc(0);
            String xml  = xmlConfig.modifyXml(xmlDesc, cpu, memory);
            Domain d = virtCon.domainDefineXML(xml);
        } catch (Exception e) {
            log.error("ERROR: modify vm error: ", e);
        }
        return true;
    }


    /**
     * 停止虚拟机
     * @param vmUuid
     */
    public boolean stopVm(String vmUuid) {
        log.info(String.format("stopVm info -- vmUuid: %s", vmUuid));
        try {
            Domain d = getDomainByUuid(vmUuid);
            try {
                if (d.isActive() > 0) {
                    d.shutdown();
                } else {
                    log.error("ERROR: the domain is already dead. shutdown failed!");
                }
                new Thread(() -> {
                    try {
                        int waitLoop = 20;
                        while (d.isActive() > 0 && waitLoop > 0) {
                            waitLoop--;
                            TimeUnit.SECONDS.sleep(1);
                        }
                        if (d.isActive() > 0) {
                            destroyDomain(d);
                        }
                    } catch (Exception e) {
                        log.error("ERROR: destroy domain error: ", e);
                    }
                }).start();
            } catch (Exception e) {
                setVmStatus(vmUuid, VmStatusEnum.ACTIVE);
                log.error("ERROR: shutdown domain error: ", e);
                return false;
            }

            // status
            log.info("-> invoke DB -- vmQueryByUuid");
            Vm vm = tableStorage.vmQueryByUuid(vmUuid);
            log.info("<- invoke DB -- done");
            vm.setStatus(VmStatusEnum.STOPPED);

            // save
            log.info("-> invoke DB -- vmSave");
            tableStorage.vmSave(vm);
            log.info("<- invoke DB -- done");

        } catch (Exception e) {
            log.error("ERROR: stop vm error: ", e);
            setVmStatus(vmUuid, VmStatusEnum.ERROR);
            return false;
        }
        return true;
    }


    /**
     * 启动虚拟机
     * @param vmUuid
     */
    public Boolean startVm(String vmUuid) {
        log.info(String.format("startVm info -- vmUuid: %s", vmUuid));
        try {
            Domain d = getDomainByUuid(vmUuid);
            try {
                if (d.isActive() == 0) {
                    d.create();
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
                        log.error("ERROR: start domain error, waiting to start ...");
                    }
                }).start();
            } catch (Exception e) {
                setVmStatus(vmUuid, VmStatusEnum.STOPPED);
                log.error("ERROR: start domain error: ", e);
                return false;
            }

            if (d.isActive() != 1) {
                log.error("ERROR: start domain error!");
            }

            // status
            log.info("-> invoke DB -- vmQueryByUuid");
            Vm vm = tableStorage.vmQueryByUuid(vmUuid);
            log.info("<- invoke DB -- done");
            vm.setStatus(VmStatusEnum.ACTIVE);

            // new vnc port
            String vncPort = ShellUtils.getCmd(LibvirtConfig.getVncPort + " " + d.getUUIDString()).replaceAll("\\r\\n|\\r|\\n|\\n\\r|:", "");          //获取新建虚拟机的VncPort
            vm.setVncPort(vncPort);

            // save
            log.info("-> invoke DB -- vmSave");
            tableStorage.vmSave(vm);
            log.info("<- invoke DB -- done");
        } catch (Exception e) {
            setVmStatus(vmUuid, VmStatusEnum.ERROR);
            log.error("ERROR: start vm error: ", e);
            return false;
        }
        return true;
    }


    /**
     * 重启虚拟机
     * @param vmUuid
     * @return
     */
    public Boolean rebootVm(String vmUuid) {
        log.info(String.format("rebootVm info -- vmUuid: %s", vmUuid));
        try {
            Domain domain = getDomainByUuid(vmUuid);
            if (domain.isActive() == 0) {
                try {
                    domain.create(0);
                    String vncPort = ShellUtils.getCmd(LibvirtConfig.getVncPort + " " + domain.getUUIDString()).replaceAll("\\r\\n|\\r|\\n|\\n\\r|:", "");      //获取新建虚拟机的VncPort
                    log.info("-> invoke DB -- vmQueryByUuid");
                    Vm vm = tableStorage.vmQueryByUuid(vmUuid);
                    log.info("<- invoke DB -- done");
                    vm.setVncPort(vncPort);
                    vm.setStatus(VmStatusEnum.ACTIVE);
                    log.info("-> invoke DB -- vmSave");
                    tableStorage.vmSave(vm);
                    log.info("<- invoke DB -- done");
                } catch (Exception e) {
                    log.error("ERROR: domain Starting failed!", e);
                    setVmStatus(vmUuid, VmStatusEnum.STOPPED);
                    return false;
                }
            } else {
                try {
                    domain.reboot(0);
                    String vncPort = ShellUtils.getCmd(LibvirtConfig.getVncPort + " " + domain.getUUIDString()).replaceAll("\\r\\n|\\r|\\n|\\n\\r|:", "");      //获取新建虚拟机的VncPort
                    log.info("-> invoke DB -- vmQueryByUuid");
                    Vm vm = tableStorage.vmQueryByUuid(vmUuid);
                    log.info("<- invoke DB -- done");
                    vm.setVncPort(vncPort);
                    vm.setStatus(VmStatusEnum.ACTIVE);
                    log.info("-> invoke DB -- vmSave");
                    tableStorage.vmSave(vm);
                    log.info("<- invoke DB -- done");
                } catch (Exception e) {
                    log.error("ERROR: domain Starting failed!", e);
                    setVmStatus(vmUuid, VmStatusEnum.ACTIVE);
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("ERROR: domain Starting failed!", e);
            setVmStatus(vmUuid, VmStatusEnum.ERROR);
            return false;
        }
        return true;
    }

    /**
     * 挂起虚拟机
     * @param vmUuid
     * @return
     */
    public Boolean suspendVm(String vmUuid) {
        log.info(String.format("suspendVm info -- vmUuid: %s", vmUuid));
        try {
            Domain domain = getDomainByUuid(vmUuid);
            if (domain.isActive() > 0) {
                try {
                    domain.managedSave();
                    setVmStatus(vmUuid, VmStatusEnum.SUSPENDED);
                } catch (Exception e) {
                    log.error("ERROR: domain suspend failed!", e);
                    setVmStatus(vmUuid, VmStatusEnum.ACTIVE);
                    return false;
                }
            } else {
                log.error("ERROR: the domain is already dead. suspend failed!");
                setVmStatus(vmUuid, VmStatusEnum.STOPPED);
                return false;
            }
        } catch (Exception e) {
            log.error("ERROR: domain Starting failed!", e);
            setVmStatus(vmUuid, VmStatusEnum.ERROR);
            return false;
        }
        return true;
    }

    /**
     * 恢复虚拟机
     * @param vmUuid
     * @return
     */
    public Boolean resumeVm(String vmUuid) {
        log.info(String.format("resumeVm info -- vmUuid: %s", vmUuid));
        try {
            Domain domain = getDomainByUuid(vmUuid);
            if (domain.isActive() > 0){
                log.error("ERROR: domain resume failed! ---- domain is still active");
                setVmStatus(vmUuid, VmStatusEnum.ACTIVE);
                return false;
            }
            domain.create(0);
            log.info("-> invoke DB -- vmQueryByUuid");
            Vm vm = tableStorage.vmQueryByUuid(vmUuid);
            log.info("<- invoke DB -- done");
            String vncPort = ShellUtils.getCmd(LibvirtConfig.getVncPort + " " + domain.getUUIDString()).replaceAll("\\r\\n|\\r|\\n|\\n\\r|:", "");      //获取新建虚拟机的VncPort
            vm.setStatus(VmStatusEnum.ACTIVE);
            vm.setVncPort(vncPort);
            log.info("-> invoke DB -- vmSave");
            tableStorage.vmSave(vm);
            log.info("<- invoke DB -- done");
        } catch (Exception e) {
            log.error("ERROR: domain resume failed! ---- domain is dead", e);
            setVmStatus(vmUuid, VmStatusEnum.STOPPED);
            return false;
        }
        return true;
    }

    public String getXmlDesc(String vmUuid) {
        log.info(String.format("getXmlDesc info -- vmUuid: %s", vmUuid));
        try {
            Domain domain = getDomainByUuid(vmUuid);
            return domain.getXMLDesc(0);
        } catch (Exception e) {
            log.error("ERROR: getXmlDesc failed!", e);
            return null;
        }
    }

    /**
     * 获取状态
     */
    public VmStatusEnum status(String vmUuid) {
        log.info(String.format("status info -- vmUuid: %s", vmUuid));
        DomainInfo.DomainState domainState;
        int hasManagedSaveImage;
        try {
            Domain domain = getDomainByUuid(vmUuid);
            domainState = domain.getInfo().state;
            hasManagedSaveImage = domain.hasManagedSaveImage();
        } catch (Exception e) {
            log.error("ERROR: status ---- unknown ----", e);
            return VmStatusEnum.UNKNOWN;
        }
        switch (domainState) {
            case VIR_DOMAIN_RUNNING:
                vncService.saveVncPort(vmUuid);
                return VmStatusEnum.ACTIVE;
            case VIR_DOMAIN_SHUTDOWN:
            case VIR_DOMAIN_SHUTOFF:
                return (1 == hasManagedSaveImage) ? VmStatusEnum.SUSPENDED : VmStatusEnum.STOPPED;
            case VIR_DOMAIN_PAUSED:
                return VmStatusEnum.PAUSED;
            case VIR_DOMAIN_BLOCKED:
            case VIR_DOMAIN_CRASHED:
            case VIR_DOMAIN_NOSTATE:
            default:
                return VmStatusEnum.ERROR;
        }
    }

    // ===== 已废弃 =====
    // 暂停虚拟机（已废弃）
    /*
    public Boolean suspendVm(String vmUuid) {
        log.info("suspendVm ---- start ---- vmUuid: " + vmUuid);
        try {
            Domain domain = getDomainByUuid(vmUuid);
            if (domain.isActive() > 0) {
                try {
                    domain.suspend();
                    setVmStatus(vmUuid, VmStatusEnum.SUSPENDED);
                } catch (Exception e) {
                    log.error("suspendVm ---- end ---- Domain suspend failed!");
                    setVmStatus(vmUuid, VmStatusEnum.ACTIVE);
                    e.printStackTrace();
                    return false;
                }
            } else {
                log.error("suspendDomain -- The domain is already dead. suspend failed!");
                setVmStatus(vmUuid, VmStatusEnum.STOPPED);
                return false;
            }
            log.info("suspendVm ---- end ---- Domain is suspending.");
        } catch (Exception e) {
            log.error("suspendVm ---- end ---- Domain Starting failed!");
            setVmStatus(vmUuid, VmStatusEnum.ERROR);
            e.printStackTrace();
            return false;
        }
        return true;
    }
     */

    // ===== 已废弃 =====
    // 恢复（暂停的虚拟机）
    /*
    public Boolean resumeVm(String vmUuid) {
        log.info("resumeVm ---- start ---- vmUuid: " + vmUuid);
        try {
            Domain domain = getDomainByUuid(vmUuid);
            if (domain.isActive() > 0){
                log.error("resumeVm ---- end ---- Domain resume failed! ---- domain is still active");
                setVmStatus(vmUuid, VmStatusEnum.ACTIVE);
                return false;
            }
            domain.resume();
            Vm vm = tableStorage.vmQueryByUuid(vmUuid);
            String vncPort = ShellUtils.getCmd(LibvirtConfig.getVncPort + " " + domain.getUUIDString()).replaceAll("\\r\\n|\\r|\\n|\\n\\r|:", "");      //获取新建虚拟机的VncPort
            vm.setStatus(VmStatusEnum.ACTIVE);
            vm.setVncPort(vncPort);
            tableStorage.vmSave(vm);
            log.info("resumeVm ---- end ---- Domain is resuming.");
        } catch (Exception e) {
            log.error("resumeVm ---- end ---- Domain resume failed! ---- domain is dead");
            setVmStatus(vmUuid, VmStatusEnum.STOPPED);
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
        log.info(String.format("destroyDomain info -- vmUuid: %s", d.getUUIDString()));

        if (d.isActive() > 0) {
            d.destroy();
        }

        if (1 == d.hasManagedSaveImage()) {
            d.managedSaveRemove();
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
                log.error("ERROR: destroyDomain ---- failed ---- ", e);
            }
        }).start();
    }

    private Domain getDomainByUuid(String vmUuid) throws Exception {
        log.info(String.format("getDomainByUuid info -- vmUuid: %s", vmUuid));
        if (vmUuid == null) {
            log.error("ERROR: vm uuid is empty");
            throw new Exception("vm uuid is empty");
        }

        Connect virtCon = LibvirtConfig.getVirtCon();
        try {
            Domain d = virtCon.domainLookupByUUIDString(vmUuid);
            if (d == null) {
                log.error(String.format("ERROR: no domain with uuid: %s", vmUuid));
                throw new Exception("no domain with uuid: " + vmUuid);
            }
            return d;
        } catch (LibvirtException e) {
            log.error("ERROR: get domain by uuid failed!", e);
            throw new Exception("get domain by uuid failed!");
        }
    }

    private void setVmStatus(String vmUuid, VmStatusEnum status){
        log.info(String.format("setVmStatus info -- vmUuid: %s, status: %s", vmUuid, status));
        log.info("-> invoke DB -- vmQueryByUuid");
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        log.info("<- invoke DB -- done");
        vm.setStatus(status);
        log.info("-> invoke DB -- vmSave");
        tableStorage.vmSave(vm);
        log.info("<- invoke DB -- done");
    }

    public boolean setPasswd(String uuid, String user, String passwd) {
        log.info(String.format("setPassword info -- uuid: %s, user: %s, password: %s", uuid, user, passwd));
        // 修改密码的操作没有封装在 Libvirt Java SDK 内，因此需要使用 virsh 命令操作
        try {
            String cmd = String.format(LibvirtConfig.SET_PASSWD, uuid, user, passwd);
            String res = ShellUtils.getCmd(cmd);
            if (res.contains(LibvirtConfig.ERROR_FLAG)) {
                log.error(String.format("ERROR: setPasswd failed %s", res));
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            log.error("ERROR: command run failed!", e);
            return false;
        }
    }
}
