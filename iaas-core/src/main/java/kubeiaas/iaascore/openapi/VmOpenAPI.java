package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.VmConstants;
import kubeiaas.common.enums.vm.VmOperateEnum;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.VmException;
import kubeiaas.iaascore.request.vm.*;
import kubeiaas.iaascore.response.*;
import kubeiaas.iaascore.service.VmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
        log.info("-- start -- create");
        Vm newVm = vmService.createVm(f.getName(), f.getCpus(), f.getMemory(), f.getImageUuid(), f.getIpSegmentId(), f.getPublicIpSegId(), f.getDiskSize(), f.getDescription(), f.getHostUuid());
        log.info("-- end -- create -- success");
        return JSON.toJSONString(BaseResponse.success(newVm));
    }

    /**
     * 删除
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DELETE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String delete(@Valid @RequestBody DeleteVmForm f) throws BaseException, VmException {
        log.info("-- start -- delete");
        String result;
        if (f.getDeleteType().equals(VmConstants.DELETE_FORCE)) {
            result = vmService.deleteVM(f.getVmUuid(), true);
        } else {
            result = vmService.deleteVM(f.getVmUuid(), false);
        }
        if (result.equals(ResponseMsgConstants.SUCCESS)) {
            log.info("-- end -- delete -- success");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- delete -- failed");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_DELETE_ERROR));
        }
    }

    /**
     * 停止
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.STOP, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String stop(@Valid @RequestBody OperateVmForm f) throws BaseException {
        log.info("-- start -- stop");
        if (vmService.operateVm(f.getVmUuid(), VmOperateEnum.STOP)
                .equals(ResponseMsgConstants.SUCCESS)){
            log.info("-- end -- stop -- success");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- stop -- failed");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_STOP_ERROR));
        }
    }

    /**
     * 启动
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.START, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String start(@Valid @RequestBody OperateVmForm f) throws BaseException {
        log.info("-- start -- start");
        if (vmService.operateVm(f.getVmUuid(), VmOperateEnum.START)
                .equals(ResponseMsgConstants.SUCCESS)) {
            log.info("-- end -- start -- success");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- start -- failed");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_START_ERROR));
        }
    }

    /**
     * 重启
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.REBOOT, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String reboot(@Valid @RequestBody OperateVmForm f) throws BaseException {
        log.info("-- start -- reboot");
        if (vmService.operateVm(f.getVmUuid(), VmOperateEnum.REBOOT)
                .equals(ResponseMsgConstants.SUCCESS)){
            log.info("-- end -- reboot -- success");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- reboot -- failed");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_REBOOT_ERROR));
        }
    }

    /**
     * 暂停
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.SUSPEND, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String suspend(@Valid @RequestBody OperateVmForm f) throws BaseException {
        log.info("-- start -- suspend");
        if (vmService.operateVm(f.getVmUuid(), VmOperateEnum.SUSPEND)
                .equals(ResponseMsgConstants.SUCCESS)) {
            log.info("-- end -- suspend -- success");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- suspend -- failed");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_SUSPEND_ERROR));
        }
    }

    /**
     * 恢复
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.RESUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String resume(@Valid @RequestBody OperateVmForm f) throws BaseException {
        log.info("-- start -- resume");
        if (vmService.operateVm(f.getVmUuid(), VmOperateEnum.RESUME)
                .equals(ResponseMsgConstants.SUCCESS)) {
            log.info("-- end -- resume -- success");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- resume -- failed");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_RESUME_ERROR));
        }
    }

    /**
     * 增加 CPU/MEM 配置
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.UPDATE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String modify(@Valid @RequestBody ModifyVmForm f) throws BaseException {
        log.info("-- start -- modify");
        if (vmService.modifyVm(f.getVmUuid(), f.getCpus(), f.getMemory(), false)
                .equals(ResponseMsgConstants.SUCCESS)){
            log.info("-- end -- modify -- success");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- modify -- failed");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_MODIFY_ERROR));
        }
    }

    /**
     * 减少 CPU/MEM 配置
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.REDUCE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String reduce(@Valid @RequestBody ModifyVmForm f) throws BaseException {
        log.info("-- start -- reduce");
        if (vmService.modifyVm(f.getVmUuid(), f.getCpus(), f.getMemory(), true)
                .equals(ResponseMsgConstants.SUCCESS)){
            log.info("-- end -- reduce -- success");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- reduce -- failed");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VM_MODIFY_ERROR));
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
        log.info("-- start -- vncUrl");
        String url = vmService.getVncUrl(uuid);
        Map<String, String> resMap = new HashMap<>();
        resMap.put(RequestMappingConstants.VNC_URL, url);
        log.info("-- end -- vncUrl -- success");
        return JSON.toJSONString(BaseResponse.success(resMap));
    }

    /**
     * 修改：名称、描述
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.EDIT, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String edit(@Valid @RequestBody EditVmForm f) throws BaseException {
        log.info("-- start -- edit");
        Vm vm = vmService.editVm(f.getVmUuid(), f.getName(), f.getDescription());
        log.info("-- end -- edit -- success");
        return JSON.toJSONString(BaseResponse.success(vm));
    }

    /**
     * 镜像发布：将系统盘发布，名称、描述
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.PUBLISH_IMAGE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String publishImage(@Valid @RequestBody PublishImageForm f) throws BaseException {
        log.info("-- start -- publishImage");
        if (vmService.publishImage(f.getVmUuid(), f.getName(), f.getDescription(), f.getVdSize())
                .equals(ResponseMsgConstants.SUCCESS)) {
            log.info("-- end -- publishImage -- success");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- publishImage -- failed");
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
        log.info("-- start -- setPasswd");
        vmService.setPasswd(f.getVmUuid(), f.getUser(), f.getPasswd());
        log.info("-- end -- setPasswd -- success");
        return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
    }

}
