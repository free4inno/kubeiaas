package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.IpUsed;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.VmConstants;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.request.vm.CreateVmForm;
import kubeiaas.iaascore.request.vm.DeleteVmForm;
import kubeiaas.iaascore.request.vm.ModifyVmForm;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.ResponseEnum;
import kubeiaas.iaascore.service.VmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.VM)
public class VmOpenAPI {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private VmService vmService;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.TEST, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String test(HttpServletRequest request) {
        log.info("test ==== start ====");
        log.info("URI " + request.getRemoteAddr() + " " + request.getRemoteHost() + " " + request.getRemotePort());
        log.info("test ==== end ====");
        return "hello";
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.CREATE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String create(@Valid @RequestBody CreateVmForm f) throws BaseException {
        log.info("create ==== start ====");
        Vm newVm = vmService.createVm(f.getName(), f.getCpus(), f.getMemory(), f.getImageUuid(), f.getIpSegmentId(), f.getDiskSize(), f.getDescription(), f.getHostUuid());
        log.info("create ==== end ====");
        return JSON.toJSONString(BaseResponse.success(newVm));
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DELETE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String delete(@Valid @RequestBody DeleteVmForm f) throws BaseException {
        log.info("delete ==== start ====");
        String result;
        if (f.getDeleteType().equals(VmConstants.DELETE_FORCE)) {
            result = vmService.forceDeleteVm(f.getVmUuid());
        } else {
            result = vmService.deleteVM(f.getVmUuid());
        }
        if (result.equals(ResponseMsgConstants.SUCCESS)) {
            log.info("delete ==== end ====");
            return JSON.toJSONString(BaseResponse.success("Delete VM Success"));
        } else {
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_DELETE_ERROR));
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.UPDATE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String modify(@Valid @RequestBody ModifyVmForm f) throws BaseException {
        log.info("modify ==== start ====");
        if (vmService.modifyVm(f.getVmUuid(), f.getCpus(), f.getMemory()).equals(ResponseMsgConstants.SUCCESS)){
            log.info("modify ==== end ====");
            return JSON.toJSONString(BaseResponse.success("Modify VM Success"));
        }else {
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_MODIFY_ERROR));
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("queryAll ==== start ====");
        List<Vm> vmList = tableStorage.vmQueryAll();
        for (Vm vm : vmList) {
            List<IpUsed> ipUsedList = tableStorage.ipUsedQueryAllByInstanceUuid(vm.getUuid());
            vm.setIps(ipUsedList);
        }
        log.info("queryAll ==== end ====");
        return JSON.toJSONString(BaseResponse.success(vmList));
    }

}
