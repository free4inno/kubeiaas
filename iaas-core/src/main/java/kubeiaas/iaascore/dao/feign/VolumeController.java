package kubeiaas.iaascore.dao.feign;

import kubeiaas.common.constants.ComponentConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URI;

//@FeignClient(name = ComponentConstants.VOLUME_CONTROLLER, url = "http://192.168.31.238:9090")
@FeignClient(name = ComponentConstants.VOLUME_CONTROLLER, url = "EMPTY")
public interface VolumeController {

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VOLUME_C + "/" + RequestMappingConstants.CREATE_SYSTEM_VOLUME)
    @ResponseBody
    String createSystemVolume(
            URI uri,
            @RequestParam(value = RequestParamConstants.IMAGE_PATH) String imagePath,
            @RequestParam(value = RequestParamConstants.VOLUME_PATH) String volumePath,
            @RequestParam(value = RequestParamConstants.VOLUME_UUID) String volumeUuid,
            @RequestParam(value = RequestParamConstants.EXTRA_SIZE) int extraSize);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VOLUME_C + "/" + RequestMappingConstants.CREATE_DATA_VOLUME)
    @ResponseBody
    String createDataVolume(
            URI uri,
            @RequestParam(value = RequestParamConstants.VOLUME_PATH) String volumePath,
            @RequestParam(value = RequestParamConstants.VOLUME_UUID) String volumeUuid,
            @RequestParam(value = RequestParamConstants.EXTRA_SIZE) int extraSize);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VOLUME_C + "/" + RequestMappingConstants.DELETE_SYSTEM_VOLUME)
    @ResponseBody
    String deleteSystemVolume(
            URI uri,
            @RequestParam(value = RequestParamConstants.VOLUME_UUID) String volumeUuid,
            @RequestParam(value = RequestParamConstants.VOLUME_PATH) String volumePath);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VOLUME_C + "/" + RequestMappingConstants.DELETE_DATA_VOLUME)
    @ResponseBody
    String deleteDataVolume(
            URI uri,
            @RequestParam(value = RequestParamConstants.VOLUME_PATH) String volumePath);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VOLUME_C + "/" + RequestMappingConstants.ATTACH_DATA_VOLUME)
    @ResponseBody
    String attachDataVolume(
            URI uri,
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObjectStr,
            @RequestParam(value = RequestParamConstants.VOLUME_OBJECT) String volumeObjectStr);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VOLUME_C + "/" + RequestMappingConstants.DETACH_DATA_VOLUME)
    @ResponseBody
    String detachDataVolume(
            URI uri,
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObjectStr,
            @RequestParam(value = RequestParamConstants.VOLUME_OBJECT) String volumeObjectStr);
}
