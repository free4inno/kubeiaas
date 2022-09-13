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

//@FeignClient(name = ComponentConstants.VM_CONTROLLER, url = "http://192.168.31.238:9090")
@FeignClient(name = ComponentConstants.VM_CONTROLLER, url = "EMPTY")
public interface VmController {

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VM_C + "/" + RequestMappingConstants.CREATE_VM_INSTANCE)
    @ResponseBody
    String createVmInstance(
            URI uri,
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid);

}
