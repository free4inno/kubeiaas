package kubeiaas.iaasagent.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.iaasagent.service.VolumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.VOLUME_C)
public class VolumeController {

    @Resource
    private VolumeService volumeService;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.CREATE_SYSTEM_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String createSystemVolume(
            @RequestParam(value = RequestParamConstants.IMAGE_PATH) String imagePath,
            @RequestParam(value = RequestParamConstants.VOLUME_PATH) String volumePath,
            @RequestParam(value = RequestParamConstants.VOLUME_UUID) String volumeUuid,
            @RequestParam(value = RequestParamConstants.EXTRA_SIZE) int extraSize) {
        log.info("-- start -- createSystemVolume");
        if (volumeService.createSystemVolume(imagePath, volumePath, volumeUuid, extraSize)) {
            log.info("-- end -- createSystemVolume -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.warn("-- end -- createSystemVolume -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.CREATE_DATA_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String createDataVolume(
            @RequestParam(value = RequestParamConstants.VOLUME_PATH) String volumePath,
            @RequestParam(value = RequestParamConstants.VOLUME_UUID) String volumeUuid,
            @RequestParam(value = RequestParamConstants.EXTRA_SIZE) int extraSize) {
        log.info("-- start -- createDataVolume");
        if (volumeService.createDataVolume(volumePath, volumeUuid, extraSize)) {
            log.info("-- end -- createDataVolume -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.warn("-- end -- createDataVolume -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.RESIZE_SYSTEM_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String resizeSystemVolume(
            @RequestParam(value = RequestParamConstants.VOLUME_PATH) String volumePath,
            @RequestParam(value = RequestParamConstants.VOLUME_UUID) String volumeUuid,
            @RequestParam(value = RequestParamConstants.DISK_SIZE) int diskSize) {
        log.info("-- start -- resizeSystemVolume");
        if (volumeService.resizeVolume(volumePath, volumeUuid, diskSize)) {
            log.info("-- end -- resizeSystemVolume -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.warn("-- end -- resizeSystemVolume -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.RESIZE_DATA_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String resizeDataVolume(
            @RequestParam(value = RequestParamConstants.VOLUME_PATH) String volumePath,
            @RequestParam(value = RequestParamConstants.VOLUME_UUID) String volumeUuid,
            @RequestParam(value = RequestParamConstants.DISK_SIZE) int diskSize) {
        log.info("-- start -- resizeDataVolume");
        if (volumeService.resizeVolume(volumePath, volumeUuid, diskSize)) {
            log.info("-- end -- resizeDataVolume -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.warn("-- end -- resizeDataVolume -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_SYSTEM_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String deleteSystemVolume(
            @RequestParam(value = RequestParamConstants.VOLUME_UUID) String volumeUuid,
            @RequestParam(value = RequestParamConstants.VOLUME_PATH) String volumePath) {
        log.info("-- start -- deleteSystemVolume");
        if (volumeService.deleteVolume(volumeUuid, volumePath)) {
            log.info("-- end -- deleteSystemVolume -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.warn("-- end -- deleteSystemVolume -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_DATA_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String deleteDataVolume(
            @RequestParam(value = RequestParamConstants.VOLUME_UUID) String volumeUuid,
            @RequestParam(value = RequestParamConstants.VOLUME_PATH) String volumePath) {
        log.info("-- start -- deleteDataVolume");
        if (volumeService.deleteVolume(volumeUuid, volumePath)) {
            log.info("-- end -- deleteDataVolume -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.warn("-- end -- deleteDataVolume -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.ATTACH_DATA_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String attachDataVolume(
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObjectStr,
            @RequestParam(value = RequestParamConstants.VOLUME_OBJECT) String volumeObjectStr) {
        log.info("-- start -- attachDataVolume");
        Vm vm = JSON.parseObject(vmObjectStr, Vm.class);
        Volume volume = JSON.parseObject(volumeObjectStr, Volume.class);
        if(volumeService.attachVolume(vm,volume)){
            log.info("-- end -- attachDataVolume -- success");
            return ResponseMsgConstants.SUCCESS;
        }else {
            log.warn("-- end -- attachDataVolume -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DETACH_DATA_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String detachDataVolume(
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObjectStr,
            @RequestParam(value = RequestParamConstants.VOLUME_OBJECT) String volumeObjectStr) {
        log.info("-- start -- detachVolume");
        Vm vm = JSON.parseObject(vmObjectStr, Vm.class);
        Volume volume = JSON.parseObject(volumeObjectStr, Volume.class);
        if(volumeService.detachVolume(vm,volume)){
            log.info("-- end -- detachVolume -- success");
            return ResponseMsgConstants.SUCCESS;
        }else {
            log.warn("-- end -- detachVolume -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VOLUME_PUBLISH_IMAGE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String volumePublishImage(
            @RequestParam(value = RequestParamConstants.VOLUME_PATH) String volumePath,
            @RequestParam(value = RequestParamConstants.IMAGE_PATH) String imagePath,
            @RequestParam(value = RequestParamConstants.EXTRA_SIZE) Integer extraSize) {
        log.info("-- start -- publishImage");
        if (null == extraSize) extraSize = 0;
        if (volumeService.volumeToImage(volumePath, imagePath, extraSize)) {
            log.info("-- end -- publishImage -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.warn("-- end -- publishImage -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DATA_VOLUME_STORAGE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String getDataVolStorage() {
        log.info("-- start -- getDataVolStorage");
        String dataDir = VolumeConstants.DEFAULT_NFS_SRV_PATH + VolumeConstants.DATA_VOLUME_PATH;
        Map<String, String> resMap = volumeService.getVolStorage(dataDir);
        log.info("-- end -- getDataVolStorage -- success");
        return JSON.toJSONString(resMap);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.IMG_VOLUME_STORAGE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String getImgVolStorage() {
        log.info("-- start -- getImgVolStorage");
        String imageDir = VolumeConstants.DEFAULT_NFS_SRV_PATH + VolumeConstants.IMAGE_PATH;
        Map<String, String> resMap = volumeService.getVolStorage(imageDir);
        log.info("-- end -- getImgVolStorage -- success");
        return JSON.toJSONString(resMap);
    }
}
