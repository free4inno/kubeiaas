package kubeiaas.iaascore.dao.feign;

import kubeiaas.common.constants.ComponentConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URI;

@FeignClient(name = ComponentConstants.DEVICE_CONTROLLER, url = "EMPTY")
public interface DeviceController {

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DEVICE_C + "/" + RequestMappingConstants.QUERY_ALL)
    @ResponseBody
    String queryAll(
            URI uri);

}
