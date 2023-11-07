package kubeiaas.iaascore.service;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.Iso;
import kubeiaas.common.bean.Mount;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.utils.PathUtils;
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
        log.info(String.format("uploadIsoToDatabase info -- isoPath: %s, isoName: %s", isoPath, isoName));
        // 1. Set iso information
        log.info("STEP 1: Set iso information");
        Iso iso = new Iso();
        iso.setUuid(UuidUtils.getRandomUuid());
        iso.setName(isoName);
        iso.setRelativePath(PathUtils.mergePaths(isoPath, isoName));
        iso.setCreateTime(new Timestamp(System.currentTimeMillis()));
        iso.setNfsPath(VolumeConstants.DEFAULT_NFS_SRV_PATH);

        // 2. Allocate host for ISO for resource scheduling
        log.info("STEP 2: Allocate host for ISO for resource scheduling");
        Host host = resourceProcess.selectOptimalHost().orElseThrow(() -> new BaseException(ResponseEnum.HOST_NOT_AVAILABLE));
        iso.setHostUuid(host.getUuid());
        AgentConfig.setSelectedHost(iso.getUuid(), host);

        // 3. Save iso information to database
        log.info("STEP 3: Save iso information to database");
        try {
            URI hostUri = new URI("http://" + host.getIp() + ":32201");
            String isoUuid = isoController.uploadIso(hostUri, JSON.toJSONString(iso));
            AgentConfig.clearSelectedHost(iso.getUuid());
            return isoUuid;
        } catch (URISyntaxException e) {
            log.error("URI construct error", e);
            throw new IsoException(ResponseEnum.ISO_UPLOAD_ERROR);
        }
    }

    public String attachIsoAsScsi(String vmUuid, String isoUuid) throws BaseException, IsoException {
        log.info(String.format("attachIsoAsScsi info -- vmUuid: %s, isoUuid: %s", vmUuid, isoUuid));
        // 1. Choose host for the vm to attach iso
        log.info("STEP 1: Choose host for the vm to attach iso");
        resourceProcess.selectHostByVmUuid(vmUuid);

        // 2. Prepare
        log.info("STEP 2: Query vm and iso information from database");
        log.info(String.format("-> invoke DB -- vmQueryByUuid. vmUuid: %s", vmUuid));
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        log.info("<- invoke DB -- done");
        log.info(String.format("-> invoke DB -- isoQueryByUuid. isoUuid: %s", isoUuid));
        Iso iso = tableStorage.isoQueryByUuid(isoUuid)
                .orElseThrow(() -> new IsoException(ResponseEnum.ISO_NOT_FOUND));
        log.info("<- invoke DB -- done");

        // 3. Configure the mount information of the iso
        log.info("STEP 3: Configure the mount information of the iso");
        Mount mount = mountProcess.processMountForScsi(vm, iso, true)
                .orElseThrow(() -> new IsoException(ResponseEnum.MOUNT_POINT_NOT_ENOUGH));

        // 4. Call iaas-agent to attach iso
        log.info("STEP 4: Call iaas-agent to attach iso");
        URI hostUri = getSelectedHostUri(vmUuid)
                .orElseThrow(() -> new BaseException(ResponseEnum.HOST_URI_NOT_FOUND));
        if (isoController.attachIso(hostUri, JSON.toJSONString(vm), JSON.toJSONString(mount))
                .equals(ResponseMsgConstants.SUCCESS)) {
            // 5. clear selected host
            log.info("STEP 5: clear selected host");
            AgentConfig.clearSelectedHost(vmUuid);

            return mount.getMountPoint();
        } else {
            // 5. clear selected host
            log.info("STEP 5: clear selected host");
            AgentConfig.clearSelectedHost(vmUuid);

            log.error(String.format("ERROR: attachIso: attach iso failed, vmUuid: %s, isoUuid: %s", vmUuid, isoUuid));
            throw new IsoException(ResponseEnum.ISO_ATTACH_ERROR);
        }
    }

    public String attachIsoAsVirtio(String vmUuid, String isoUuid) throws BaseException, IsoException {
        log.info(String.format("attachIsoAsVirtio info -- vmUuid: %s, isoUuid: %s", vmUuid, isoUuid));
        // 1. Choose host for the vm to attach iso
        log.info("STEP 1: Choose host for the vm to attach iso");
        resourceProcess.selectHostByVmUuid(vmUuid);

        // 2. Prepare
        log.info("STEP 2: Query vm and iso information from database");
        log.info(String.format("-> invoke DB -- vmQueryByUuid. vmUuid: %s", vmUuid));
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        log.info("<- invoke DB -- done");
        log.info(String.format("-> invoke DB -- isoQueryByUuid. isoUuid: %s", isoUuid));
        Iso iso = tableStorage.isoQueryByUuid(isoUuid)
                .orElseThrow(() -> new IsoException(ResponseEnum.ISO_NOT_FOUND));
        log.info("<- invoke DB -- done");

        // 3. Configure the mount information of the iso
        log.info("STEP 3: Configure the mount information of the iso");
        Mount mount = mountProcess.processMountForVirtio(vm, iso, true)
                        .orElseThrow(() -> new IsoException(ResponseEnum.ISO_ATTACH_ERROR));

        // 4. Call iaas-agent to attach iso
        log.info("STEP 4: Call iaas-agent to attach iso");
        URI hostUri = getSelectedHostUri(vmUuid)
                .orElseThrow(() -> new BaseException(ResponseEnum.HOST_URI_NOT_FOUND));
        if (isoController.attachIso(hostUri, JSON.toJSONString(vm), JSON.toJSONString(mount))
                .equals(ResponseMsgConstants.SUCCESS)) {
            // 5. clear selected host
            log.info("STEP 5: clear selected host");
            AgentConfig.clearSelectedHost(vmUuid);

            return mount.getMountPoint();
        } else {
            // 5. clear selected host
            log.info("STEP 5: clear selected host");
            AgentConfig.clearSelectedHost(vmUuid);

            log.error(String.format("ERROR: attachIso: attach iso failed, vmUuid: %s, isoUuid: %s", vmUuid, isoUuid));
            throw new IsoException(ResponseEnum.ISO_ATTACH_ERROR);
        }
    }

    public String detachIso(String vmUuid, String isoUuid) throws BaseException, IsoException {
        log.info(String.format("detachIso info -- vmUuid: %s, isoUuid: %s", vmUuid, isoUuid));
        // 1. Prepare
        log.info("STEP 1: Query vm and mount information from database");
        log.info(String.format("-> invoke DB -- vmQueryByUuid. vmUuid: %s", vmUuid));
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        log.info("<- invoke DB -- done");
        log.info(String.format("-> invoke DB -- queryMountsByVmUuidAndIsoUuid. vmUuid: %s, isoUuid: %s", vmUuid, isoUuid));
        List<Mount> mounts = tableStorage.queryMountsByVmUuidAndIsoUuid(vmUuid, isoUuid);
        log.info("<- invoke DB -- done");

        log.info("STEP 2: Iterate the mount list and detach iso");
        List<Mount> detachedMounts = new ArrayList<>();
        for (Mount mount : mounts) {
            // 3. Choose host for the vm to detach iso
            log.info("STEP 3: Choose host for the vm to detach iso");
            resourceProcess.selectHostByVmUuid(vmUuid);

            // 4. Call iaas-agent to detach iso
            log.info("STEP 4: Call iaas-agent to detach iso");
            URI hostUri = getSelectedHostUri(vmUuid)
                    .orElseThrow(() -> new BaseException(ResponseEnum.HOST_URI_NOT_FOUND));
            if (!isoController.detachIso(hostUri, JSON.toJSONString(vm), JSON.toJSONString(mount))
                    .equals(ResponseMsgConstants.SUCCESS)) {

                // 5. clear selected host
                log.info("STEP 5: clear selected host");
                AgentConfig.clearSelectedHost(vmUuid);

                log.error(String.format("ERROR: detachIso: detach iso failed, vmUuid: %s, isoUuid: %s", vmUuid, isoUuid));
                throw new IsoException(ResponseEnum.ISO_DETACH_ERROR);
            }
            // 5. clear selected host
            log.info("STEP 5: clear selected host");
            AgentConfig.clearSelectedHost(vmUuid);

            // 6. Update mount status
            log.info("STEP 6: Update mount status");
            detachedMounts.add(mount);
        }

        return JSON.toJSONString(detachedMounts);
    }

    public String queryByUuid(String isoUuid) throws BaseException, IsoException {
        log.info(String.format("queryByUuid info -- isoUuid: %s", isoUuid));
        log.info(String.format("-> invoke DB -- isoQueryByUuid. isoUuid: %s", isoUuid));
        Iso iso = tableStorage.isoQueryByUuid(isoUuid)
                .orElseThrow(() -> new IsoException(ResponseEnum.ISO_NOT_FOUND));
        log.info("<- invoke DB -- done");
        return JSON.toJSONString(iso);
    }

    Optional<URI> getSelectedHostUri(String vmUuid) {
        log.info(String.format("getSelectedHostUri info -- vmUuid: %s", vmUuid));
        try {
            return Optional.of(new URI(AgentConfig.getSelectedUri(vmUuid)));
        } catch (URISyntaxException e) {
            log.error(String.format("ERROR: getSelectedHostUri: vmUuid %s not found", vmUuid), e);
            return Optional.empty();
        }
    }
}
