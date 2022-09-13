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

}
