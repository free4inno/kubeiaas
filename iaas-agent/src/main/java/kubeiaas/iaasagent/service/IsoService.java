package kubeiaas.iaasagent.service;

import kubeiaas.common.bean.Iso;
import kubeiaas.common.bean.Mount;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.commands.CommonCommands;
import kubeiaas.common.commands.response.CommandResponse;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.utils.ShellUtils;
import kubeiaas.common.utils.UuidUtils;
import kubeiaas.iaasagent.config.LibvirtConfig;
import kubeiaas.iaasagent.config.XmlConfig;
import kubeiaas.iaasagent.dao.TableStorage;
import kubeiaas.iaasagent.utils.FetchUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.management.monitor.StringMonitor;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
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

    public Boolean uploadIso(String name, String fileUrl) {
        log.info(String.format("uploadIso info -- name: %s, fileUrl: %s", name, fileUrl));
        try {
            InetAddress zhiAddress = InetAddress.getByName("zhi.free4inno.com");
            log.info(String.format("zhiAddress: %s", zhiAddress));
        } catch (UnknownHostException e) {
            log.error("ERROR: get zhiAddress failed", e);
        }
        // 1. Download iso file
        URL url;
        fileUrl = fileUrl.trim();
        try {
            url = new URL(fileUrl);
        } catch (MalformedURLException e) {
            log.error("ERROR: file url is invalid", e);
            return false;
        }

        File targetFile;
        try {
            Optional<File> fileOpt = FetchUtils.fetchFile(url, VolumeConstants.DEFAULT_NFS_SRV_PATH + VolumeConstants.ISO_PATH, name);
            if (!fileOpt.isPresent()) {
                log.error("ERROR: fetch file failed");
                return false;
            }
            targetFile = fileOpt.get();
        } catch (IOException e) {
            log.error("ERROR: fetch file stream failed", e);
            return false;
        }

        // 2. Save iso info to DB
        Iso iso = new Iso();
        iso.setName(name);
        iso.setUuid(UuidUtils.getRandomUuid());
        iso.setRelativePath(name);
        iso.setSize(targetFile.length());
        iso.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
        log.info(String.format("-> invoke DB -- isoSave. iso: %s", iso));
        tableStorage.isoSave(iso);
        log.info("<- invoke DB -- done");

        return true;
    }

    public boolean deleteIso(String uuid) {
        Iso iso = tableStorage.isoQueryByUuid(uuid).orElseThrow(() -> new RuntimeException("iso not found"));
        File isoFile = new File(iso.getAbsolutePath());
        if (isoFile.exists() && !isoFile.delete()) {
            log.error(String.format("ERROR: delete tmp file failed, tmpFile: %s", isoFile.getAbsolutePath()));
            return false;
        }

        tableStorage.deleteIsoByUuid(uuid);
        return true;
    }
}
