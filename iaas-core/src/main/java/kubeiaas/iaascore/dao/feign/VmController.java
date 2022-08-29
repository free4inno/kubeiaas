package kubeiaas.iaascore.dao.feign;

import kubeiaas.common.constants.ComponentConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = ComponentConstants.VM_CONTROLLER, url = "http://127.0.0.1:9090")
public interface VmController {

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VM_C + "/" + RequestMappingConstants.CREATE_VM_INSTANCE)
    @ResponseBody
    String createVmInstance(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid);

}
