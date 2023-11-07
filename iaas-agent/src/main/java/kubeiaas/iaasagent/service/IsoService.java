package kubeiaas.iaasagent.service;

import kubeiaas.common.bean.Iso;
import kubeiaas.common.bean.Mount;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.commands.CommonCommands;
import kubeiaas.common.commands.response.CommandResponse;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.utils.ShellUtils;
import kubeiaas.iaasagent.config.LibvirtConfig;
import kubeiaas.iaasagent.config.XmlConfig;
import kubeiaas.iaasagent.dao.TableStorage;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.management.monitor.StringMonitor;
import java.util.Optional;

@Slf4j
@Service
public class IsoService {
    @Resource
    private XmlConfig xmlConfig;

    @Resource
    private TableStorage tableStorage;

    public Boolean attachIso(Vm vm, Mount mount) {
        log.info(String.format("attachIso info -- vm: %s, mount: %s", vm, mount));
        // 1. Get the xml of the attachment
        String isoMountXml = xmlConfig.getMountXml(mount);

        String vmUuid = vm.getUuid();
        // 2. Call libvirt to attach iso
        try {
            Connect virtCon = LibvirtConfig.getVirtCon();
            Domain domain = virtCon.domainLookupByUUIDString(vmUuid);
            try {
                /*
                 * Attach a virtual device to a domain, using the flags parameter to control how the device is attached.
                 * - 0000: VIR_DOMAIN_AFFECT_CURRENT specifies that the device allocation is made based on current domain state.
                 * - 0001: VIR_DOMAIN_AFFECT_LIVE specifies that the device shall be allocated to the active domain instance only and is not added to the persisted domain configuration.
                 * - 0010: VIR_DOMAIN_AFFECT_CONFIG specifies that the device shall be allocated to the persisted domain configuration only. Note that the target hypervisor must return an error if unable to satisfy flags.
                 * - 0100: FORCE
                 * Use | to combine those configs, we got 3 as (VIR_DOMAIN_AFFECT_LIVE | VIR_DOMAIN_AFFECT_CONFIG)
                 */
                int virtFlag = vm.getStatus().equals(VmStatusEnum.ACTIVE) ? (0b0001 | 0b0010) : (0b0010);
                domain.attachDeviceFlags(isoMountXml, virtFlag);
            } catch (Exception e) {
                log.error("ERROR: attachIso: attach iso failed", e);
                return false;
            }
        } catch (LibvirtException e) {
            log.error("ERROR: attachIso: get virtCon failed", e);
            return false;
        }
        log.info(String.format("-> invoke DB -- mountSave. mount: %s", mount));
        tableStorage.mountSave(mount);
        log.info("<- invoke DB -- done");
        return true;
    }

    public boolean detachIso(Vm vm, Mount mount) {
        log.info(String.format("detachIso info -- vm: %s, mount: %s", vm, mount));
        // 1. Get the xml of the detachment
        String isoMountXml = xmlConfig.getMountXml(mount);

        String vmUuid = vm.getUuid();
        try {
            Connect virtCon = LibvirtConfig.getVirtCon();
            Domain domain = virtCon.domainLookupByUUIDString(vmUuid);
            try {
                /*
                 * Detach a virtual device from a domain, using the flags parameter to control how the device is detached.
                 * - 0000: VIR_DOMAIN_AFFECT_CURRENT specifies that the device allocation is made based on current domain state.
                 * - 0001: VIR_DOMAIN_AFFECT_LIVE specifies that the device shall be allocated to the active domain instance only and is not added to the persisted domain configuration.
                 * - 0010: VIR_DOMAIN_AFFECT_CONFIG specifies that the device shall be allocated to the persisted domain configuration only. Note that the target hypervisor must return an error if unable to satisfy flags.
                 * - 0100: FORCE
                 * Use | to combine those configs, we got 3 as (VIR_DOMAIN_AFFECT_LIVE | VIR_DOMAIN_AFFECT_CONFIG)
                 */
                int virtFlag = vm.getStatus().equals(VmStatusEnum.ACTIVE) ? (0b0001 | 0b0010) : (0b0010);
                domain.detachDeviceFlags(isoMountXml, virtFlag);
            } catch (Exception e) {
                log.error("ERROR: detachIso: detach iso failed", e);
                return false;
            }
        } catch (LibvirtException e) {
            log.error("ERROR: detachIso: get virtCon failed", e);
            return false;
        }
        log.info(String.format("-> invoke DB -- mountDeleteById. mountID: %d", mount.getId()));
        Integer id = tableStorage.mountDeleteById(mount.getId());
        log.info(String.format("<- invoke DB -- done. mountID(deleted): %d", id));
        return true;
    }

    public Boolean uploadIso(Iso iso) {
        log.info(String.format("uploadIso info -- iso: %s", iso));
        log.info(String.format("-> invoke DB -- isoSave. iso: %s", iso));
        Iso savedIso = tableStorage.isoSave(iso);
        log.info(String.format("<- invoke DB -- done. savedIso: %s", savedIso));
        return true;
    }

    public boolean isIsoExistsInServer(String isoPath) {
        log.info(String.format("isIsoExistsInServer info -- isoPath: %s", isoPath));
        String cmd = String.format(CommonCommands.SHELL_IS_FILE_EXIST, isoPath);
        String result = ShellUtils.getCmd(cmd);
        if (result.equals(CommandResponse.TRUE)) {
            return true;
        } else if (result.equals(CommandResponse.FALSE)) {
            return false;
        } else {
            log.error(String.format("ERROR: get iso exist failed, isoPath: %s", isoPath));
            return false;
        }
    }

    public Optional<Long> getIsoSizeInBytes(String isoPath) {
        log.info(String.format("getIsoSizeInBytes info -- isoPath: %s", isoPath));
        String cmd = String.format(CommonCommands.SHELL_GET_FILE_SIZE_IN_BYTES, isoPath);
        String result = ShellUtils.getCmd(cmd);
        // if result is not number, return empty optional else return size
        try {
            return Optional.of(Long.parseLong(result));
        } catch (NumberFormatException e) {
            log.error(String.format("ERROR: get iso size failed, isoPath: %s", isoPath), e);
            return Optional.empty();
        }
    }
}
