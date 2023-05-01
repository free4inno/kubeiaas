package kubeiaas.dbproxy.controller;

import kubeiaas.common.constants.RequestMappingConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
public class HeartbeatController {
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.HEARTBEAT, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String heartbeat() {
        log.info("heartbeat...");
        return "heartbeat";
    }
}