package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.LogInjectionConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.VmConstants;
import kubeiaas.common.enums.vm.VmBootEnum;
import kubeiaas.common.enums.vm.VmOperateEnum;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.VmException;
import kubeiaas.iaascore.request.vm.*;
import kubeiaas.iaascore.response.*;
import kubeiaas.iaascore.service.VmService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@Controller
@RequestMapping(value = RequestMappingConstants.VM)
public class VmOpenAPI {

    @Resource
    private VmService vmService;

    /**
     * 创建
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.CREATE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String create(@Valid @RequestBody CreateVmForm f) throws VmException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getImageUuid());
        log.info("-- start -- create");
        Vm newVm = vmService.createVm(f.getName(), f.getCpus(), f.getMemory(), f.getImageUuid(), f.getIpSegmentId(), f.getPublicIpSegId(), f.getDiskSize(), f.getDescription(), f.getHostUuid());
        log.info("-- end -- create -- success");
        MDC.remove(LogInjectionConstants.UUID_INJECTION);
        return JSON.toJSONString(BaseResponse.success(newVm));
    }

    /**
     * 删除
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DELETE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String delete(@Valid @RequestBody DeleteVmForm f) throws BaseException, VmException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVmUuid());
        log.info("-- start -- delete");
        String result;
        if (f.getDeleteType().equals(VmConstants.DELETE_FORCE)) {
            result = vmService.deleteVM(f.getVmUuid(), true);
        } else {
            result = vmService.deleteVM(f.getVmUuid(), false);
        }
        String responseMsg;
        if (result.equals(ResponseMsgConstants.SUCCESS)) {
            log.info("-- end -- delete -- success");
            responseMsg = JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- delete -- failed");
            responseMsg = JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_DELETE_ERROR));
        }
        MDC.remove(LogInjectionConstants.UUID_INJECTION);
        return responseMsg;
    }

    /**
     * 停止
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.STOP, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String stop(@Valid @RequestBody OperateVmForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVmUuid());
        log.info("-- start -- stop");
        String responseMsg;
        if (vmService.operateVm(f.getVmUuid(), VmOperateEnum.STOP)
                .equals(ResponseMsgConstants.SUCCESS)){
            log.info("-- end -- stop -- success");
            responseMsg = JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- stop -- failed");
            responseMsg = JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_STOP_ERROR));
        }
        MDC.remove(LogInjectionConstants.UUID_INJECTION);
        return responseMsg;
    }

    /**
     * 启动
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.START, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String start(@Valid @RequestBody OperateVmForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVmUuid());
        log.info("-- start -- start");
        String responseMsg;
        if (vmService.operateVm(f.getVmUuid(), VmOperateEnum.START)
                .equals(ResponseMsgConstants.SUCCESS)) {
            log.info("-- end -- start -- success");
            responseMsg = JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- start -- failed");
            responseMsg = JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_START_ERROR));
        }
        MDC.remove(LogInjectionConstants.UUID_INJECTION);
        return responseMsg;
    }

    /**
     * 重启
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.REBOOT, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String reboot(@Valid @RequestBody OperateVmForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVmUuid());
        log.info("-- start -- reboot");
        String responseMsg;
        if (vmService.operateVm(f.getVmUuid(), VmOperateEnum.REBOOT)
                .equals(ResponseMsgConstants.SUCCESS)){
            log.info("-- end -- reboot -- success");
            responseMsg = JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- reboot -- failed");
            responseMsg = JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_REBOOT_ERROR));
        }
        MDC.remove(LogInjectionConstants.UUID_INJECTION);
        return responseMsg;
    }

    /**
     * 暂停
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.SUSPEND, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String suspend(@Valid @RequestBody OperateVmForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVmUuid());
        log.info("-- start -- suspend");
        if (vmService.operateVm(f.getVmUuid(), VmOperateEnum.SUSPEND)
                .equals(ResponseMsgConstants.SUCCESS)) {
            log.info("-- end -- suspend -- success");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- suspend -- failed");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_SUSPEND_ERROR));
        }
    }

    /**
     * 恢复
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.RESUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String resume(@Valid @RequestBody OperateVmForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVmUuid());
        log.info("-- start -- resume");
        if (vmService.operateVm(f.getVmUuid(), VmOperateEnum.RESUME)
                .equals(ResponseMsgConstants.SUCCESS)) {
            log.info("-- end -- resume -- success");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- resume -- failed");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_RESUME_ERROR));
        }
    }

    /**
     * 增加 CPU/MEM 配置
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.UPDATE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String modify(@Valid @RequestBody ModifyVmForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVmUuid());
        log.info("-- start -- modify");
        if (vmService.modifyVm(f.getVmUuid(), f.getCpus(), f.getMemory(), false)
                .equals(ResponseMsgConstants.SUCCESS)){
            log.info("-- end -- modify -- success");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- modify -- failed");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_MODIFY_ERROR));
        }
    }

    /**
     * 减少 CPU/MEM 配置
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.REDUCE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String reduce(@Valid @RequestBody ModifyVmForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVmUuid());
        log.info("-- start -- reduce");
        if (vmService.modifyVm(f.getVmUuid(), f.getCpus(), f.getMemory(), true)
                .equals(ResponseMsgConstants.SUCCESS)){
            log.info("-- end -- reduce -- success");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- reduce -- failed");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_MODIFY_ERROR));
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.COLD_MIGRATE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String coldMigrate(@Valid @RequestBody ColdMigrateVmForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVmUuid());
        log.info("-- start -- coldMigrate");
        if (vmService.coldMigrateVm(f.getVmUuid(), f.getTargetHostUuid(), f.getTargetPrivateIpSegmentId(), f.getTargetPublicIpSegmentId())
                .equals(ResponseMsgConstants.SUCCESS)){
            log.info("-- end -- coldMigrate -- success");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- coldMigrate -- failed");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_COLD_MIGRATE_ERROR));
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = RequestMappingConstants.LIVE_MIGRATE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String liveMigrate(@Valid @RequestBody LiveMigrateVmForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVmUuid());
        log.info("-- start -- liveMigrate");
        if (vmService.liveMigrateVm(f.getVmUuid(), f.getTargetHostUuid())
                .equals(ResponseMsgConstants.SUCCESS)){
            log.info("-- end -- liveMigrate -- success");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- liveMigrate -- failed");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_LIVE_MIGRATE_ERROR));
        }
    }

    /**
     * 获取 vm 列表
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("-- start -- queryAll");
        List<Vm> vmList = vmService.queryAll();
        log.info("-- end -- queryAll -- success");
        return JSON.toJSONString(BaseResponse.success(vmList));
    }

    /**
     * 分页获取 vm 列表
     * @return pageTotal, vmTotal, vmList
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.PAGE_QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String pageQueryAll(
            @RequestParam(value = RequestParamConstants.PAGE_NUM) @NotNull @Min(1) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) @NotNull @Min(1) Integer pageSize) {
        log.info("-- start -- pageQueryAll");
        PageResponse<Vm> res = vmService.pageQueryAll(pageNum, pageSize);
        log.info("-- end -- pageQueryAll -- success");
        return JSON.toJSONString(BaseResponse.success(res));
    }

    /**
     * 分页模糊搜索
     * @return pageTotal, vmTotal, vmList
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.FUZZY_QUERY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String fuzzyQuery(
            @RequestParam(value = RequestParamConstants.KEYWORDS) String keywords,
            @RequestParam(value = RequestParamConstants.STATUS) String status,
            @RequestParam(value = RequestParamConstants.HOST_UUID) String hostUuid,
            @RequestParam(value = RequestParamConstants.IMAGE_UUID) String imageUuid,
            @RequestParam(value = RequestParamConstants.PAGE_NUM) @NotNull @Min(1) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) @NotNull @Min(1) Integer pageSize) {
        log.info("-- start -- fuzzyQuery");
        PageResponse<Vm> res = vmService.fuzzyQuery(keywords, status, hostUuid, imageUuid, pageNum, pageSize);
        log.info("-- end -- fuzzyQuery -- success");
        return JSON.toJSONString(BaseResponse.success(res));
    }

    /**
     * 分页模糊搜索（可进行挂载的云主机）
     * @return pageTotal, vmTotal, vmList
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.FUZZY_QUERY_ATTACH, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String fuzzyQueryAttach(
            @RequestParam(value = RequestParamConstants.KEYWORDS) String keywords,
            @RequestParam(value = RequestParamConstants.PAGE_NUM) @NotNull @Min(1) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) @NotNull @Min(1) Integer pageSize) {
        log.info("-- start -- fuzzyQueryAttach");
        PageResponse<Vm> res = vmService.fuzzyQueryAttach(keywords, pageNum, pageSize);
        log.info("-- end -- fuzzyQueryAttach -- success");
        return JSON.toJSONString(BaseResponse.success(res));
    }

    /**
     * 获取 vm 详情
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_BY_UUID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryByUuid(
            @RequestParam(value = RequestParamConstants.UUID) @NotEmpty @NotNull String uuid) throws BaseException {
        log.info("-- start -- queryByUuid");
        Vm vm = vmService.queryByUuid(uuid);
        log.info("-- end -- queryByUuid -- success");
        return JSON.toJSONString(BaseResponse.success(vm));
    }

    /**
     * 获取 vnc 访问链接
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VNC_URL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String vncUrl(
            @RequestParam(value = RequestParamConstants.UUID) @NotEmpty @NotNull String uuid) {
        MDC.put(LogInjectionConstants.UUID_INJECTION, uuid);
        log.info("-- start -- vncUrl");
        String url = vmService.getVncUrl(uuid);
        Map<String, String> resMap = new HashMap<>();
        resMap.put(RequestMappingConstants.VNC_URL, url);
        log.info("-- end -- vncUrl -- success");
        MDC.remove(LogInjectionConstants.UUID_INJECTION);
        return JSON.toJSONString(BaseResponse.success(resMap));
    }

    /**
     * 修改：名称、描述
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.EDIT, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String edit(@Valid @RequestBody EditVmForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVmUuid());
        log.info("-- start -- edit");
        Vm vm = vmService.editVm(f.getVmUuid(), f.getName(), f.getDescription());
        log.info("-- end -- edit -- success");
        MDC.remove(LogInjectionConstants.UUID_INJECTION);
        return JSON.toJSONString(BaseResponse.success(vm));
    }

    /**
     * 镜像发布：将系统盘发布，名称、描述
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.PUBLISH_IMAGE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String publishImage(@Valid @RequestBody PublishImageForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVmUuid());
        log.info("-- start -- publishImage");
        if (vmService.publishImage(f.getVmUuid(), f.getName(), f.getDescription(), f.getVdSize())
                .equals(ResponseMsgConstants.SUCCESS)) {
            log.info("-- end -- publishImage -- success");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- publishImage -- failed");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.PUBLISH_IMAGE_ERROR));
        }
    }

    /**
     * 统计信息
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.STATISTICS, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String statistics() {
        log.info("-- start -- statistics");
        Map<String, Integer> resMap = vmService.getStatistics();
        log.info("-- end -- statistics -- success");
        return JSON.toJSONString(BaseResponse.success(resMap));
    }

    /**
     * 刷新状态
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.STATUS, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String status(
            @RequestParam(value = RequestParamConstants.UUID) @NotEmpty @NotNull String uuid) throws BaseException {
        log.info("-- start -- status");
        VmStatusEnum vmStatusEnum = vmService.status(uuid);
        log.info("-- end -- status -- success");
        return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(vmStatusEnum.toString())));
    }

    /**
     * 修改密码
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.SET_PASSWD, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String setPasswd(
            @Valid @RequestBody SetPasswdForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVmUuid());
        log.info("-- start -- setPasswd");
        vmService.setPasswd(f.getVmUuid(), f.getUser(), f.getPasswd());
        log.info("-- end -- setPasswd -- success");
        MDC.remove(LogInjectionConstants.UUID_INJECTION);
        return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
    }

    @RequestMapping(method = RequestMethod.PUT, value = RequestMappingConstants.SET_VM_BOOT_ORDER, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String setVmBootOrder(@Valid @RequestBody SetVmBootOrderForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVmUuid());
        log.info("-- start -- setVmBootOrder");
        vmService.setVmBootOrder(f.getVmUuid(), f.getBootOrder());
        log.info("-- end -- setVmBootOrder -- success");
        MDC.remove(LogInjectionConstants.UUID_INJECTION);
        return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.GET_VM_BOOT_ORDER, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String getVmBootOrder(@RequestParam(value = RequestParamConstants.UUID) @NotEmpty @NotNull String uuid) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, uuid);
        log.info("-- start -- getVmBootOrder");
        VmBootEnum bootOrder = vmService.getVmBootOrder(uuid);
        log.info("-- end -- getVmBootOrder -- success");
        MDC.remove(LogInjectionConstants.UUID_INJECTION);
        return JSON.toJSONString(BaseResponse.success(bootOrder.toString()));
    }
}
