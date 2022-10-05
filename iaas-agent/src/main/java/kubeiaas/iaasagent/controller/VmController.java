package kubeiaas.iaasagent.controller;

import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.iaasagent.service.VmService;
import kubeiaas.iaasagent.service.VolumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.VM_C)
public class VmController {

    @Resource
    private VmService vmService;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.CREATE_VM_INSTANCE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String createVmInstance(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid) {
        if (vmService.createVm(vmUuid)) {
            log.info("createVmInstance -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("createVmInstance -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_VM_INSTANCE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String deleteVm(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid) {
        log.info("deleteVm ==== start ==== vmUuid: " + vmUuid);
        // log.info("This is Vm Controller: " + routingKey);
        if (vmService.deleteVm(vmUuid)){
            log.info("deleteVmInstance -- success");
            return ResponseMsgConstants.SUCCESS;
        }else {
            log.error("deleteVmInstance -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.MODIFY_VM_INSTANCE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String modifyVm(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid) {
        log.info("modifyVm ==== start ==== vmUuid: " + vmUuid);
        if (vmService.modifyVm(vmUuid)){
            log.info("modifyVm ==== success");
            return ResponseMsgConstants.SUCCESS;
        }else{
            log.info("modifyVm ==== failed");
            return ResponseMsgConstants.FAILED;
        }
    }
}
