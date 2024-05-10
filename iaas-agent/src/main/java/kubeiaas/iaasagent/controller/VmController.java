package kubeiaas.iaasagent.controller;

import com.alibaba.fastjson2.JSON;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.iaasagent.service.VmService;
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
        log.info("-- start -- createVmInstance");
        if (vmService.createVm(vmUuid)) {
            log.info("-- end -- createVmInstance -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("-- end -- createVmInstance -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.CREATE_VM_INSTANCE_BY_XML, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String createVmInstanceByXml(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid,
            @RequestParam(value = RequestParamConstants.XML_DESC) String xmlDesc) {
        log.info("-- start -- createVmFromXml");
        if (vmService.createVmByXml(vmUuid, xmlDesc)) {
            log.info("-- end -- createVmFromXml -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("-- end -- createVmFromXml -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = RequestMappingConstants.SET_VM_BOOT_ORDER, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String setVmBootOrder(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid,
            @RequestParam(value = RequestParamConstants.BOOT_ORDER) String bootOrder) {
        log.info("-- start -- setVmBootOrder");
        if (vmService.setVmBootOrder(vmUuid, bootOrder)) {
            log.info("-- end -- setVmBootOrder -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("-- end -- setVmBootOrder -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_VM_INSTANCE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String deleteVm(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid) {
        log.info("-- start -- deleteVmInstance");
        if (vmService.deleteVm(vmUuid)) {
            log.info("-- end -- deleteVmInstance -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("-- end -- deleteVmInstance -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.STOP_VM_INSTANCE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String stopVm(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid) {
        log.info("-- start -- stopVmInstance");
        if (vmService.stopVm(vmUuid)) {
            log.info("-- end -- stopVmInstance -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("-- end -- stopVmInstance -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.START_VM_INSTANCE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String startVm(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid) {
        log.info("-- start -- startVmInstance");
        if (vmService.startVm(vmUuid)) {
            log.info("-- end -- startVmInstance -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("-- end -- startVmInstance -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.REBOOT_VM_INSTANCE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String rebootVm(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid) {
        log.info("-- start -- rebootVmInstance");
        if (vmService.rebootVm(vmUuid)) {
            log.info("-- end -- rebootVmInstance -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("-- end -- rebootVmInstance -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SUSPEND_VM_INSTANCE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String suspendVm(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid) {
        log.info("-- start -- suspendVmInstance");
        if (vmService.suspendVm(vmUuid)) {
            log.info("-- end -- suspendVmInstance -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("-- end -- suspendVmInstance -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.RESUME_VM_INSTANCE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String resumeVm(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid) {
        log.info("-- start -- resumeVmInstance");
        if (vmService.resumeVm(vmUuid)){
            log.info("-- end -- resumeVmInstance -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("-- end -- resumeVmInstance -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.MODIFY_VM_INSTANCE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String modifyVm(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid,
            @RequestParam(value = RequestParamConstants.CPUS) Integer cpus,
            @RequestParam(value = RequestParamConstants.MEMORY) Integer memory) {
        log.info("-- start -- modifyVm");
        if (vmService.modifyVm(vmUuid, cpus, memory)) {
            log.info("-- end -- modifyVm -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.info("-- end -- modifyVm -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.GET_XML_DESC, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String getXmlDesc(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid) {
        log.info("-- end -- getXmlDesc -- success");
        return vmService.getXmlDesc(vmUuid);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.STATUS, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String status(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid) {
        log.info("-- end -- status -- success");
        return JSON.toJSONString(vmService.status(vmUuid));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SET_PASSWD, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    String setPasswd(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid,
            @RequestParam(value = RequestParamConstants.USER) String user,
            @RequestParam(value = RequestParamConstants.PASSWD) String passwd) {
        log.info("-- start -- setPasswd");
        if (vmService.setPasswd(vmUuid, user, passwd)) {
            log.info("-- end -- setPasswd -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.info("-- end -- setPasswd -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }
}
