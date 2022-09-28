package kubeiaas.iaasagent.controller;

import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.iaasagent.service.HostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.HOST_C)
public class HostController {

    @Resource
    private HostService hostService;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.CHECK_ENV, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String checkEnv(
            @RequestParam(value = RequestParamConstants.TYPE) String type) {
        return "";
    }

}
