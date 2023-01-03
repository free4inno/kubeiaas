package kubeiaas.resourceoperator.register;

import kubeiaas.common.constants.ComponentConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = ComponentConstants.IAAS_CORE, url = "http://iaas-core:9080")
public interface Register {
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SERVICE + "/" + RequestMappingConstants.REGISTER)
    @ResponseBody
    String register(
            @RequestParam(value = RequestParamConstants.SERVICE_NAME) String serviceName,
            @RequestParam(value = RequestParamConstants.NODE_NAME) String nodeName,
            @RequestParam(value = RequestParamConstants.TIMESTAMP) Long timestamp
    );
}
