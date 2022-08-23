package kubeiaas.iaascore.openapi;

import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.VM)
public class VmController {

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.TEST, produces = {"application/json", "application/xml"})
    @ResponseBody
    public String test(HttpServletRequest request) {
        log.info("test ==== start ====");
        log.info("URI " + request.getRemoteAddr() + " " + request.getRemoteHost() + " " + request.getRemotePort());
        log.info("test ==== end ====");
        return "hello";
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.CREATE, produces = {"application/json", "application/xml"})
    @ResponseBody
    public void create(
            @RequestParam(value = RequestParamConstants.NAME) String name,
            @RequestParam(value = RequestParamConstants.CPUS) String cpus,
            @RequestParam(value = RequestParamConstants.MEMORY) String memory,
            @RequestParam(value = RequestParamConstants.IMAGE_UUID) String imageUuid,
            @RequestParam(value = RequestParamConstants.DISK_SIZE, required = false) String diskSize,
            @RequestParam(value = RequestParamConstants.DESCRIPTION, required = false) String description,
            @RequestParam(value = RequestParamConstants.HOST_UUID, required = false) String hostUuid) {
        log.info("create ==== start ====");

        log.info("create ==== end ====");
    }


}
