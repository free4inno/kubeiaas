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
        log.info("createSystemVolume ==== " + "IMAGE_PATH: " + imagePath + " VOLUME_PATH: " + volumePath + " VOLUME_UUID: " + volumeUuid);
        if (volumeService.createSystemVolume(imagePath, volumePath, volumeUuid, extraSize)) {
            log.info("createSystemVolume -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("createSystemVolume -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.CREATE_DATA_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String createDataVolume(
            @RequestParam(value = RequestParamConstants.VOLUME_PATH) String volumePath,
            @RequestParam(value = RequestParamConstants.VOLUME_UUID) String volumeUuid,
            @RequestParam(value = RequestParamConstants.EXTRA_SIZE) int extraSize) {
        log.info("createDataVolume ==== "  + " VOLUME_PATH: " + volumePath + " VOLUME_UUID: " + volumeUuid);
        if (volumeService.createDataVolume(volumePath, volumeUuid, extraSize)) {
            log.info("createDataVolume -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("createDataVolume -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_SYSTEM_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String deleteSystemVolume(
            @RequestParam(value = RequestParamConstants.VOLUME_UUID) String volumeUuid,
            @RequestParam(value = RequestParamConstants.VOLUME_PATH) String volumePath) {
        log.info("deleteSystemVolume ==== " + " VOLUME_PATH: " + volumePath);
        if (volumeService.deleteVolume(volumeUuid, volumePath)) {
            log.info("deleteSystemVolume -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("deleteSystemVolume -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_DATA_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String deleteDataVolume(
            @RequestParam(value = RequestParamConstants.VOLUME_UUID) String volumeUuid,
            @RequestParam(value = RequestParamConstants.VOLUME_PATH) String volumePath) {
        log.info("deleteDataVolume ==== " + " VOLUME_PATH: " + volumePath);
        if (volumeService.deleteVolume(volumeUuid, volumePath)) {
            log.info("deleteDataVolume -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("deleteDataVolume -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.ATTACH_DATA_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String attachDataVolume(
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObjectStr,
            @RequestParam(value = RequestParamConstants.VOLUME_OBJECT) String volumeObjectStr) {
        log.info("attachVolume ==== start ==== volume: " + volumeObjectStr + " vm: " + vmObjectStr);
        Vm vm = JSON.parseObject(vmObjectStr, Vm.class);
        Volume volume = JSON.parseObject(volumeObjectStr, Volume.class);
        if(volumeService.attachVolume(vm,volume)){
            log.info("attachDataVolume -- success");
            return ResponseMsgConstants.SUCCESS;
        }else {
            log.info("attachDataVolume -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DETACH_DATA_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String detachDataVolume(
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObjectStr,
            @RequestParam(value = RequestParamConstants.VOLUME_OBJECT) String volumeObjectStr) {
        log.info("detachVolume ==== start ==== volume: " + volumeObjectStr + " vm: " + vmObjectStr);
        Vm vm = JSON.parseObject(vmObjectStr, Vm.class);
        Volume volume = JSON.parseObject(volumeObjectStr, Volume.class);
        if(volumeService.detachVolume(vm,volume)){
            log.info("detachVolume -- success");
            return ResponseMsgConstants.SUCCESS;
        }else {
            log.info("detachVolume -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VOLUME_PUBLISH_IMAGE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String volumePublishImage(
            @RequestParam(value = RequestParamConstants.VOLUME_PATH) String volumePath,
            @RequestParam(value = RequestParamConstants.IMAGE_PATH) String imagePath) {
        log.info("publishImage ==== start ====");
        if (volumeService.volumeToImage(volumePath, imagePath)) {
            log.info("publishImage -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("publishImage -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DATA_VOLUME_STORAGE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String getDataVolStorage() {
        log.info("getDataVolStorage ==== start ====");
        String dataDir = VolumeConstants.DEFAULT_NFS_SRV_PATH + VolumeConstants.DATA_VOLUME_PATH;
        Map<String, String> resMap = volumeService.getVolStorage(dataDir);
        log.info("getDataVolStorage ==== end ====");
        return JSON.toJSONString(resMap);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.IMG_VOLUME_STORAGE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String getImgVolStorage() {
        log.info("getImgVolStorage ==== start ====");
        String imageDir = VolumeConstants.DEFAULT_NFS_SRV_PATH + VolumeConstants.IMAGE_PATH;
        Map<String, String> resMap = volumeService.getVolStorage(imageDir);
        log.info("getImgVolStorage ==== end ====");
        return JSON.toJSONString(resMap);
    }
}
