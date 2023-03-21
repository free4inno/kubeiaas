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

//@FeignClient(name = ComponentConstants.VM_CONTROLLER, url = "http://192.168.31.238:9090")
@FeignClient(name = ComponentConstants.VM_CONTROLLER, url = "EMPTY")
public interface VmController {

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VM_C + "/" + RequestMappingConstants.CREATE_VM_INSTANCE)
    @ResponseBody
    String createVmInstance(
            URI uri,
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VM_C + "/" + RequestMappingConstants.DELETE_VM_INSTANCE)
    @ResponseBody
    String deleteVmInstance(
            URI uri,
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VM_C + "/" + RequestMappingConstants.STOP_VM_INSTANCE)
    @ResponseBody
    String stopVmInstance(
            URI uri,
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VM_C + "/" + RequestMappingConstants.START_VM_INSTANCE)
    @ResponseBody
    String startVmInstance(
            URI uri,
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VM_C + "/" + RequestMappingConstants.REBOOT_VM_INSTANCE)
    @ResponseBody
    String rebootVmInstance(
            URI uri,
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VM_C + "/" + RequestMappingConstants.SUSPEND_VM_INSTANCE)
    @ResponseBody
    String suspendVmInstance(
            URI uri,
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VM_C + "/" + RequestMappingConstants.RESUME_VM_INSTANCE)
    @ResponseBody
    String resumeVmInstance(
            URI uri,
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VM_C + "/" + RequestMappingConstants.MODIFY_VM_INSTANCE)
    @ResponseBody
    String modifyVmInstance(
            URI uri,
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid,
            @RequestParam(value = RequestParamConstants.CPUS) Integer cpus,
            @RequestParam(value = RequestParamConstants.MEMORY) Integer memory);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VM_C + "/" + RequestMappingConstants.STATUS)
    @ResponseBody
    String status(
            URI uri,
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid);

}
