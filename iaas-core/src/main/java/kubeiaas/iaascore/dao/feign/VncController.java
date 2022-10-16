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

@FeignClient(name = ComponentConstants.VNC_CONTROLLER, url = "EMPTY")
public interface VncController {

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VNC_C + "/" + RequestMappingConstants.ADD_VNC_TOKEN)
    @ResponseBody
    void addVncToken(
            URI uri,
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid,
            @RequestParam(RequestParamConstants.ADDRESS) String address);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VNC_C + "/" + RequestMappingConstants.DELETE_VNC_TOKEN)
    @ResponseBody
    void deleteVncToken(
            URI uri,
            @RequestParam(RequestParamConstants.VM_UUID) String uuid);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VNC_C + "/" + RequestMappingConstants.FLUSH_VNC_TOKEN)
    @ResponseBody
    void flushVncToken(
            URI uri,
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid,
            @RequestParam(RequestParamConstants.ADDRESS) String address);
}
