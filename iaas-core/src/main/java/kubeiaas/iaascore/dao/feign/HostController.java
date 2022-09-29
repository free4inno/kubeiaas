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

@FeignClient(name = ComponentConstants.HOST_CONTROLLER, url = "EMPTY")
public interface HostController {

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.HOST_C + "/" + RequestMappingConstants.CONFIG_ENV)
    @ResponseBody
    String configEnv(
            URI uri,
            @RequestParam(value = RequestParamConstants.TYPE) String type);
}
