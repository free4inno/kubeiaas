package kubeiaas.iaascore.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.Iso;
import kubeiaas.common.bean.Mount;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.HufuServiceConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.utils.FileUtils;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

    public String uploadFromZhi(Integer templateId) throws BaseException, IsoException {
        log.info(String.format("uploadFromZhi info -- templateId: %d", templateId));
        // 1. Get iso information from zhi.free4inno.com
        String zhiUrl = String.format("%s/openapi/detail?appkey=sYu1gpFF&id=%d", HufuServiceConstants.TEMPLATE_CENTER_URL, templateId);
        log.info(String.format("zhi.free4inno.com -- url: %s", zhiUrl));
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(zhiUrl, String.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            log.error(String.format("ERROR: get iso info from zhi.free4inno.com failed, templateId: %d", templateId));
            throw new IsoException(ResponseEnum.ISO_UPLOAD_ERROR);
        }
        JSONObject zhiResponse = JSON.parseObject(responseEntity.getBody());
        int code = zhiResponse.getInteger("code");
        if (code != 200) {
            log.error(String.format("ERROR: get iso info from zhi.free4inno.com failed, templateId: %d", templateId));
            throw new IsoException(ResponseEnum.ISO_UPLOAD_ERROR);
        }
        JSONObject data = zhiResponse.getJSONObject("data");
        String attachmentJsonString = data.getString("attachment");
        JSONArray attachmentArray = JSON.parseArray(attachmentJsonString);
        for (int i = 0; i < attachmentArray.size(); i++) {
            JSONObject attachment = attachmentArray.getJSONObject(i);
            String name = attachment.getString("name");
            log.info(attachment.toJSONString());
            if (FileUtils.isIsoFile(name)) {
                Host host = resourceProcess.selectOptimalHost().orElseThrow(() -> new BaseException(ResponseEnum.HOST_NOT_AVAILABLE));
                String fileUrl = String.format("%s%s&name=%s", HufuServiceConstants.TEMPLATE_CENTER_URL, attachment.getString("url"), name);

                File targetFile;
                try {
                    URI hostUri = new URI("http://" + host.getIp() + ":32201");
                    log.info(String.format("uploadFromZhi info -- hostUri: %s, name: %s, fileUrl: %s", hostUri, name, fileUrl));
                    URL url = new URL(fileUrl);
                    Optional<File> fileOpt = FileUtils.fetchFile(url, VolumeConstants.DEFAULT_NFS_SRV_PATH + VolumeConstants.ISO_PATH, name);
                    if (!fileOpt.isPresent()) {
                        log.error("ERROR: fetch file failed");
                        throw new IsoException(ResponseEnum.ISO_UPLOAD_ERROR);
                    }
                    targetFile = fileOpt.get();
                } catch (URISyntaxException e) {
                    log.error("ERROR: URI construct error", e);
                    throw new IsoException(ResponseEnum.ISO_UPLOAD_ERROR);
                } catch (IOException e) {
                    log.error("ERROR: fetch file stream failed", e);
                    throw new RuntimeException(e);
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
            }
        }

        return null;
    }

    public String uploadFromLocal(String fileName) throws BaseException, IsoException {
        log.info(String.format("uploadFromLocal info -- fileName: %s", fileName));
        // 1. Check file
        File file = new File( VolumeConstants.DEFAULT_NFS_SRV_PATH + VolumeConstants.ISO_PATH + File.separator + fileName);
        if (!file.exists() || !file.isFile() || !FileUtils.isIsoFile(fileName)) {
            log.error(String.format("ERROR: uploadFromLocal: file not supported , fileName: %s", fileName));
            throw new IsoException(ResponseEnum.ISO_UPLOAD_ERROR);
        }

        // 2. Save iso info to DB
        Iso iso = new Iso();
        iso.setName(fileName);
        iso.setUuid(UuidUtils.getRandomUuid());
        iso.setRelativePath(fileName);
        iso.setSize(file.length());
        iso.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
        log.info(String.format("-> invoke DB -- isoSave. iso: %s", iso));
        tableStorage.isoSave(iso);
        log.info("<- invoke DB -- done");

        return null;
    }


    public String deleteIso(String uuid) throws BaseException, IsoException {
        log.info(String.format("deleteIso info -- uuid: %s", uuid));
        Host host = resourceProcess.selectOptimalHost().orElseThrow(() -> new BaseException(ResponseEnum.HOST_NOT_AVAILABLE));
        try {
            URI hostUri = new URI("http://" + host.getIp() + ":32201");
            if (!isoController.deleteIso(hostUri, uuid).equals(ResponseMsgConstants.SUCCESS)) {
                log.error(String.format("ERROR: deleteIso: delete iso failed, uuid: %s", uuid));
                throw new IsoException(ResponseEnum.ISO_DELETE_ERROR);
            }
        } catch (URISyntaxException e) {
            log.error("ERROR: URI construct error", e);
            throw new IsoException(ResponseEnum.ISO_DELETE_ERROR);
        }

        return null;
    }

    public String attachIso(String vmUuid, String isoUuid, String method) throws BaseException, IsoException {
        log.info(String.format("attachIso info -- vmUuid: %s, isoUuid: %s", vmUuid, isoUuid));
        // 1. Choose host for the vm to attach iso
        log.info("STEP 1: Choose host for the vm to attach iso");
        resourceProcess.selectHostByVmUuid(vmUuid);

        // 2. Configure the mount information of the iso
        log.info("STEP 2: Configure the mount information of the iso");
        log.info(String.format("-> invoke DB -- vmQueryByUuid. vmUuid: %s", vmUuid));
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        log.info("<- invoke DB -- done");
        log.info(String.format("-> invoke DB -- isoQueryByUuid. isoUuid: %s", isoUuid));
        Iso iso = tableStorage.isoQueryByUuid(isoUuid)
                .orElseThrow(() -> new IsoException(ResponseEnum.ISO_NOT_FOUND));
        log.info("<- invoke DB -- done");
        Mount mount;
        if (method.equals("virtio")) {
            mount = mountProcess.processMountForVirtio(vm, iso, true)
                    .orElseThrow(() -> new IsoException(ResponseEnum.MOUNT_POINT_NOT_ENOUGH));
        } else if (method.equals("scsi")) {
            mount = mountProcess.processMountForScsi(vm, iso, true)
                    .orElseThrow(() -> new IsoException(ResponseEnum.MOUNT_POINT_NOT_ENOUGH));
        } else {
            log.error(String.format("ERROR: attachIso: attach iso failed, vmUuid: %s, isoUuid: %s", vmUuid, isoUuid));
            throw new IsoException(ResponseEnum.ISO_ATTACH_ERROR);
        }

        // 3. Call iaas-agent to attach iso
        log.info("STEP 3: Call iaas-agent to attach iso");
        URI hostUri = getSelectedHostUri(vmUuid)
                .orElseThrow(() -> new BaseException(ResponseEnum.HOST_URI_NOT_FOUND));
        if (isoController.attachIso(hostUri, JSON.toJSONString(vm), JSON.toJSONString(mount)).equals(ResponseMsgConstants.SUCCESS)) {
            // 4. clear selected host
            log.info("STEP 4: clear selected host");
            AgentConfig.clearSelectedHost(vmUuid);

            return mount.getMountPoint();
        } else {
            // 4. clear selected host
            log.info("STEP 4: clear selected host");
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

    public String queryAll() throws BaseException, IsoException {
        log.info("queryAll info");
        log.info("-> invoke DB -- isoQueryAll");
        List<Iso> isoList = tableStorage.isoQueryAll();
        log.info("<- invoke DB -- done");
        return JSON.toJSONString(isoList);
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
