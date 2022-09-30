package kubeiaas.iaasagent.controller;

import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.iaasagent.service.VncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.VNC_C)
public class VncController {

    @Resource
    private VncService vncService;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.ADD_VNC_TOKEN, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public void addVncToken(
            @RequestParam(RequestParamConstants.VM_UUID) String uuid,
            @RequestParam(RequestParamConstants.ADDRESS) String address) {
        log.info("addVncToken ==== start ==== uuid:" + uuid + " address:" + address);
        vncService.addVncToken(uuid, address);
        log.info("addVncToken ==== end ==== ");
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_VNC_TOKEN, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public void deleteVncToken(
            @RequestParam(RequestParamConstants.VM_UUID) String uuid) {
        log.info("addVncToken ==== start ==== uuid:" + uuid);
        vncService.deleteVncToken(uuid);
        log.info("addVncToken ==== end ==== ");
    }
}
