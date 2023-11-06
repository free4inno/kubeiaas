package kubeiaas.iaascore.dao.feign;

import kubeiaas.common.constants.ComponentConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.net.URI;

@FeignClient(name = ComponentConstants.ISO_CONTROLLER, url = "EMPTY")
public interface IsoController {
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.ISO_C + "/" + RequestMappingConstants.ATTACH_ISO)
    @ResponseBody
    String attachIso(
            URI uri,
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObjectStr,
            @RequestParam(value = RequestParamConstants.MOUNT_OBJECT) String mountObjectStr
    );

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.ISO_C + "/" + RequestMappingConstants.DETACH_ISO, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    String detachIso(
            URI uri,
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObjectStr,
            @RequestParam(value = RequestParamConstants.MOUNT_OBJECT) String mountObjectStr);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.ISO_C + "/" + RequestMappingConstants.UPLOAD_ISO)
    @ResponseBody
    String uploadIso(
            URI uri,
            @RequestParam(value = RequestParamConstants.ISO_OBJECT) String isoObjectStr
    );
}
