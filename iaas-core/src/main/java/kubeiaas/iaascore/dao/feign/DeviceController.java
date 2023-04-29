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

@FeignClient(name = ComponentConstants.DEVICE_CONTROLLER, url = "EMPTY")
public interface DeviceController {

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DEVICE_C + "/" + RequestMappingConstants.QUERY_ALL)
    @ResponseBody
    String queryAll(
            URI uri);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DEVICE_C + "/" + RequestMappingConstants.ATTACH)
    @ResponseBody
    String attach(
            @RequestParam(value = RequestParamConstants.DEVICE_OBJECT) String deviceObjectStr,
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObject,
            URI uri
    );

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DEVICE_C + "/" + RequestMappingConstants.DETACH)
    @ResponseBody
    String detach(
            @RequestParam(value = RequestParamConstants.DEVICE_OBJECT) String deviceObjectStr,
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObject,
            URI uri
    );

}
