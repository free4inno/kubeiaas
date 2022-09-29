package kubeiaas.iaasagent.controller;

import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.enums.host.HostStatusEnum;
import kubeiaas.iaasagent.config.HostConfig;
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

    @Resource
    private HostConfig hostConfig;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.CONFIG_ENV, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public void configEnv(
            @RequestParam(value = RequestParamConstants.TYPE) String type) {
        log.info("configEnv ==== start ==== type:" + type);
        // write `CONFIGURING` into DB
        hostService.setHostStatus(HostStatusEnum.CONFIGURING);
        // do check but not return res
        hostConfig.checkHostEnvAsync(type);
        log.info("configEnv ==== end ====");
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.CHECK_ENV, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public void checkEnv(
            @RequestParam(value = RequestParamConstants.TYPE) String type) {
        log.info("checkEnv ==== start ==== type:" + type);
        // write `CHECKING` into DB
        hostService.setHostStatus(HostStatusEnum.CHECKING);
        // do check but not return res
        hostConfig.checkHostEnvAsync(type);
        log.info("checkEnv ==== end ====");
    }
}
