package kubeiaas.iaascore.service;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.Iso;
import kubeiaas.common.bean.Mount;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.commands.CommonCommands;
import kubeiaas.common.commands.response.CommandResponse;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.volume.VolumeFormatEnum;
import kubeiaas.common.utils.PathUtils;
import kubeiaas.common.utils.ShellUtils;
import kubeiaas.common.utils.UuidUtils;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.dao.feign.IsoController;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.IsoException;
import kubeiaas.iaascore.process.MountProcess;
import kubeiaas.iaascore.process.ResourceProcess;
import kubeiaas.iaascore.response.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class IsoService {
    @Resource
    private TableStorage tableStorage;

    @Resource
    private ResourceProcess resourceProcess;

    @Resource
    private IsoController isoController;

    @Resource
    private MountProcess mountProcess;

    public String uploadIsoToDatabase(String isoPath, String isoName) throws BaseException, IsoException {
        // 1. Set iso information
        Iso iso = new Iso();
        iso.setUuid(UuidUtils.getRandomUuid());
        iso.setName(isoName);
//        String isoRelativePath = getRelativePath(isoPath, isoName);
//        if (isIsoExistsInServer(isoRelativePath)) {
//            iso.setRelativePath(isoRelativePath);
//        } else {
//            log.error("uploadIsoToDatabase: iso file not found, isoPath: {}, isoName: {}", isoPath, isoName);
//            throw new IsoException(ResponseEnum.ISO_FILE_NOT_FOUND);
//        }
//        iso.setSize(getIsoSizeInBytes(isoRelativePath)
//                .orElseThrow(() -> new IsoException(ResponseEnum.ISO_FILE_NOT_FOUND)));
        iso.setRelativePath(PathUtils.mergePaths(isoPath, isoName));

        iso.setCreateTime(new Timestamp(System.currentTimeMillis()));
        iso.setNfsPath(VolumeConstants.DEFAULT_NFS_SRV_PATH);

        // 2. Allocate host for ISO for resource scheduling
        Host host = resourceProcess.selectOptimalHost().orElseThrow(() -> new BaseException(ResponseEnum.HOST_NOT_AVAILABLE));
        iso.setHostUuid(host.getUuid());
        AgentConfig.setSelectedHost(iso.getUuid(), host);
        log.info("uploadIsoToDatabase: select host for iso {}, host: {}", isoName, host);

        // 3. Save iso information to database
//        Iso savedIso = tableStorage.isoSave(iso);
        try {
            URI hostUri = new URI("http://" + host.getIp() + ":32201");
            String isoUuid = isoController.uploadIso(hostUri, JSON.toJSONString(iso));
            log.info("uploadIsoToDatabase: save iso to database, iso: {}", isoUuid);
            AgentConfig.clearSelectedHost(iso.getUuid());
            return isoUuid;
        } catch (URISyntaxException e) {
            log.error("URI construct error", e);
            throw new IsoException(ResponseEnum.ISO_UPLOAD_ERROR);
        }
    }

    public String attachIsoAsScsi(String vmUuid, String isoUuid) throws BaseException, IsoException {
        // 1. Choose host for the vm to attach iso
        resourceProcess.selectHostByVmUuid(vmUuid);

        // 2. Prepare
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        Iso iso = tableStorage.isoQueryByUuid(isoUuid)
                .orElseThrow(() -> new IsoException(ResponseEnum.ISO_NOT_FOUND));

        // 3. Configure the mount information of the iso
        Mount mount = mountProcess.processMountForScsi(vm, iso, true)
                .orElseThrow(() -> new IsoException(ResponseEnum.MOUNT_POINT_NOT_ENOUGH));

        // 4. Call iaas-agent to attach iso
        URI hostUri = getSelectedHostUri(vmUuid)
                .orElseThrow(() -> new BaseException(ResponseEnum.HOST_URI_NOT_FOUND));
        if (isoController.attachIso(hostUri, JSON.toJSONString(vm), JSON.toJSONString(mount))
                .equals(ResponseMsgConstants.SUCCESS)) {
            // 5. clear selected host
            AgentConfig.clearSelectedHost(vmUuid);

            return mount.getMountPoint();
        } else {
            // 5. clear selected host
            AgentConfig.clearSelectedHost(vmUuid);

            log.error("attachIso: attach iso failed, vmUuid: {}, isoUuid: {}", vmUuid, isoUuid);
            throw new IsoException(ResponseEnum.ISO_ATTACH_ERROR);
        }
    }

    public String attachIsoAsVirtio(String vmUuid, String isoUuid) throws BaseException, IsoException {
        // 1. Choose host for the vm to attach iso
        resourceProcess.selectHostByVmUuid(vmUuid);

        // 2. Prepare
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        Iso iso = tableStorage.isoQueryByUuid(isoUuid)
                .orElseThrow(() -> new IsoException(ResponseEnum.ISO_NOT_FOUND));

        // 3. Configure the mount information of the iso
        Mount mount = mountProcess.processMountForVirtio(vm, iso, true)
                        .orElseThrow(() -> new IsoException(ResponseEnum.ISO_ATTACH_ERROR));

        // 4. Call iaas-agent to attach iso
        URI hostUri = getSelectedHostUri(vmUuid)
                .orElseThrow(() -> new BaseException(ResponseEnum.HOST_URI_NOT_FOUND));
        if (isoController.attachIso(hostUri, JSON.toJSONString(vm), JSON.toJSONString(mount))
                .equals(ResponseMsgConstants.SUCCESS)) {
            // 5. clear selected host
            AgentConfig.clearSelectedHost(vmUuid);

            return mount.getMountPoint();
        } else {
            // 5. clear selected host
            AgentConfig.clearSelectedHost(vmUuid);

            log.error("attachIso: attach iso failed, vmUuid: {}, isoUuid: {}", vmUuid, isoUuid);
            throw new IsoException(ResponseEnum.ISO_ATTACH_ERROR);
        }
    }

    public String detachIso(String vmUuid, String isoUuid) throws BaseException, IsoException {
        // 1. Prepare
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);

        // 2.
        List<Mount> mounts = tableStorage.queryMountsByVmUuidAndIsoUuid(vmUuid, isoUuid);
        List<Mount> detachedMounts = new ArrayList<>();
        for (Mount mount : mounts) {
            // 3. Choose host for the vm to detach iso
            resourceProcess.selectHostByVmUuid(vmUuid);

            // 4. Call iaas-agent to detach iso
            URI hostUri = getSelectedHostUri(vmUuid)
                    .orElseThrow(() -> new BaseException(ResponseEnum.HOST_URI_NOT_FOUND));
            if (!isoController.detachIso(hostUri, JSON.toJSONString(vm), JSON.toJSONString(mount))
                    .equals(ResponseMsgConstants.SUCCESS)) {

                // 5. clear selected host
                AgentConfig.clearSelectedHost(vmUuid);

                log.error("detachIso: detach iso failed, vmUuid: {}, isoUuid: {}", vmUuid, isoUuid);
                throw new IsoException(ResponseEnum.ISO_DETACH_ERROR);
            }
            // 5. clear selected host
            AgentConfig.clearSelectedHost(vmUuid);

            // 6. Update mount status
            detachedMounts.add(mount);
        }

        return JSON.toJSONString(detachedMounts);
    }

    public String queryByUuid(String isoUuid) throws BaseException, IsoException {
        Iso iso = tableStorage.isoQueryByUuid(isoUuid)
                .orElseThrow(() -> new IsoException(ResponseEnum.ISO_NOT_FOUND));
        return JSON.toJSONString(iso);
    }

    Optional<URI> getSelectedHostUri(String vmUuid) {
        try {
            return Optional.of(new URI(AgentConfig.getSelectedUri(vmUuid)));
        } catch (URISyntaxException e) {
            log.error("getSelectedHostUri: select host by vm uuid failed, vmUuid: {}", vmUuid, e);
            return Optional.empty();
        }
    }
}
