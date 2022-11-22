package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.VmConstants;
import kubeiaas.common.enums.vm.VmOperateEnum;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.VmException;
import kubeiaas.iaascore.request.vm.*;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.ResponseEnum;
import kubeiaas.iaascore.service.VmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.VM)
public class VmOpenAPI {

    @Resource
    private VmService vmService;

    /**
     * 测试接口
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.TEST, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String test(HttpServletRequest request) {
        log.info("test ==== start ====");
        log.info("URI " + request.getRemoteAddr() + " " + request.getRemoteHost() + " " + request.getRemotePort());
        log.info("test ==== end ====");
        return "hello";
    }

    /**
     * 创建
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.CREATE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String create(@Valid @RequestBody CreateVmForm f) throws VmException {
        log.info("create ==== start ====");
        Vm newVm = vmService.createVm(f.getName(), f.getCpus(), f.getMemory(), f.getImageUuid(), f.getIpSegmentId(), f.getDiskSize(), f.getDescription(), f.getHostUuid());
        log.info("create ==== end ====");
        return JSON.toJSONString(BaseResponse.success(newVm));
    }

    /**
     * 删除
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DELETE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String delete(@Valid @RequestBody DeleteVmForm f) throws BaseException, VmException {
        log.info("delete ==== start ====");
        String result;
        if (f.getDeleteType().equals(VmConstants.DELETE_FORCE)) {
            result = vmService.deleteVM(f.getVmUuid(), true);
        } else {
            result = vmService.deleteVM(f.getVmUuid(), false);
        }
        if (result.equals(ResponseMsgConstants.SUCCESS)) {
            Map<String, String> resData = new HashMap<>();
            resData.put("message", "Delete VM Success");
            log.info("delete ==== end ====");
            return JSON.toJSONString(BaseResponse.success(resData));
        } else {
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_DELETE_ERROR));
        }
    }

    /**
     * 停止
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.STOP, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String stop(@Valid @RequestBody OperateVmForm f) throws BaseException {
        log.info("stop ==== start ====");
        if (vmService.operateVm(f.getVmUuid(), VmOperateEnum.STOP).equals(ResponseMsgConstants.SUCCESS)){
            Map<String, String> resData = new HashMap<>();
            resData.put("message", "Stop VM Success");
            log.info("stop ==== end ====");
            return JSON.toJSONString(BaseResponse.success(resData));
        } else {
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_STOP_ERROR));
        }
    }

    /**
     * 启动
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.START, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String start(@Valid @RequestBody OperateVmForm f) throws BaseException {
        log.info("startVm ==== start ====");
        if (vmService.operateVm(f.getVmUuid(), VmOperateEnum.START).equals(ResponseMsgConstants.SUCCESS)){
            Map<String, String> resData = new HashMap<>();
            resData.put("message", "Start VM Success");
            log.info("startVm ==== end ====");
            return JSON.toJSONString(BaseResponse.success(resData));
        } else {
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_START_ERROR));
        }
    }

    /**
     * 重启
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.REBOOT, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String reboot(@Valid @RequestBody OperateVmForm f) throws BaseException {
        log.info("rebootVm  ==== start ====");
        if (vmService.operateVm(f.getVmUuid(), VmOperateEnum.REBOOT).equals(ResponseMsgConstants.SUCCESS)){
            Map<String, String> resData = new HashMap<>();
            resData.put("message", "Reboot VM Success");
            log.info("rebootVm ==== end ====");
            return JSON.toJSONString(BaseResponse.success(resData));
        } else {
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_REBOOT_ERROR));
        }
    }

    /**
     * 暂停
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.SUSPEND, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String suspend(@Valid @RequestBody OperateVmForm f) throws BaseException {
        log.info("suspendVm  ==== start ====");
        if (vmService.operateVm(f.getVmUuid(), VmOperateEnum.SUSPEND).equals(ResponseMsgConstants.SUCCESS)){
            Map<String, String> resData = new HashMap<>();
            resData.put("message", "Suspend VM Success");
            log.info("suspendVm ==== end ====");
            return JSON.toJSONString(BaseResponse.success(resData));
        } else {
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_SUSPEND_ERROR));
        }
    }

    /**
     * 恢复
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.RESUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String resume(@Valid @RequestBody OperateVmForm f) throws BaseException {
        log.info("resumeVm  ==== start ====");
        if (vmService.operateVm(f.getVmUuid(), VmOperateEnum.RESUME).equals(ResponseMsgConstants.SUCCESS)){
            Map<String, String> resData = new HashMap<>();
            resData.put("message", "Resume VM Success");
            log.info("resumeVm ==== end ====");
            return JSON.toJSONString(BaseResponse.success(resData));
        } else {
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_RESUME_ERROR));
        }
    }

    /**
     * 增加 cpu/mem 配置
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.UPDATE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String modify(@Valid @RequestBody ModifyVmForm f) throws BaseException {
        log.info("modify ==== start ====");
        if (vmService.modifyVm(f.getVmUuid(), f.getCpus(), f.getMemory(), false).equals(ResponseMsgConstants.SUCCESS)){
            Map<String, String> resData = new HashMap<>();
            resData.put("message", "Modify VM Success");
            log.info("modify ==== end ====");
            return JSON.toJSONString(BaseResponse.success(resData));
        } else {
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_MODIFY_ERROR));
        }
    }

    /**
     * 减少 cpu/mem 配置
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.REDUCE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String reduce(@Valid @RequestBody ModifyVmForm f) throws BaseException {
        log.info("reduce ==== start ====");
        if (vmService.modifyVm(f.getVmUuid(), f.getCpus(), f.getMemory(), true).equals(ResponseMsgConstants.SUCCESS)){
            Map<String, String> resData = new HashMap<>();
            resData.put("message", "reduce VM Success");
            log.info("reduce ==== end ====");
            return JSON.toJSONString(BaseResponse.success(resData));
        } else {
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_MODIFY_ERROR));
        }
    }

    /**
     * 获取 vm 列表
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("queryAll ==== start ====");
        List<Vm> vmList = vmService.queryAll();
        log.info("queryAll ==== end ====");
        return JSON.toJSONString(BaseResponse.success(vmList));
    }

    /**
     * 获取 vm 详情
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_BY_UUID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryByUuid(
            @RequestParam(value = RequestParamConstants.UUID) @NotEmpty @NotNull String uuid) {
        log.info("queryByUuid ==== start ====");
        Vm vm = vmService.queryByUuid(uuid);
        log.info("queryByUuid ==== end ====");
        return JSON.toJSONString(BaseResponse.success(vm));
    }

    /**
     * 获取 vnc 访问链接
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VNC_URL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String vncUrl(
            @RequestParam(value = RequestParamConstants.UUID) @NotEmpty @NotNull String uuid) {
        String url = vmService.getVncUrl(uuid);
        Map<String, String> resMap = new HashMap<>();
        resMap.put(RequestMappingConstants.VNC_URL, url);
        return JSON.toJSONString(BaseResponse.success(resMap));
    }

    /**
     * 修改：名称、描述
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.EDIT, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String edit(@Valid @RequestBody EditVmForm f) throws BaseException {
        log.info("edit ==== start ====");
        Vm vm = vmService.editVm(f.getVmUuid(), f.getName(), f.getDescription());
        log.info("edit ==== end ====");
        return JSON.toJSONString(BaseResponse.success(vm));
    }

}
