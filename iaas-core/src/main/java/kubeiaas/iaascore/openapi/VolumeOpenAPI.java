package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson2.JSON;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.LogInjectionConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.VolumeException;
import kubeiaas.iaascore.request.volume.*;
import kubeiaas.iaascore.response.*;
import kubeiaas.iaascore.service.VolumeService;
import kubeiaas.iaascore.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;


@Slf4j
@Validated
@Controller
@RequestMapping(value = RequestMappingConstants.VOLUME)
public class VolumeOpenAPI {

    @Resource
    private VolumeService volumeService;

    /**
     * 创建 “云硬盘”
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.CREATE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String create(@Valid @RequestBody CreateVolumeForm f) throws VolumeException {
        log.info("-- start -- createDataVolume");
        Volume newVolume = volumeService.createDataVolume(f.getName(), f.getDiskSize(), f.getDescription(), f.getHostUuid());
        log.info("-- end -- createDataVolume -- success");
        MDC.remove(LogInjectionConstants.UUID_INJECTION);
        return JSON.toJSONString(BaseResponse.success(newVolume));
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.RESIZE_SYSTEM_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String resizeSystemVolume(@Valid @RequestBody ResizeVolumeForm f) throws VolumeException, BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVolumeUuid());
        log.info("-- start -- resizeSystemVolume");
        String result = volumeService.resizeSystemVolume(f.getVolumeUuid(), f.getDiskSize());
        if (result.equals(ResponseMsgConstants.SUCCESS)) {
            log.info("-- end -- resizeSystemVolume -- success");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- resizeSystemVolume -- failed");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VOLUME_RESIZE_ERROR));
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.RESIZE_DATA_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String resizeDataVolume(@Valid @RequestBody ResizeVolumeForm f) throws VolumeException, BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVolumeUuid());
        log.info("-- start -- resizeDataVolume");
        String result = volumeService.resizeDataVolume(f.getVolumeUuid(), f.getDiskSize());
        if (result.equals(ResponseMsgConstants.SUCCESS)) {
            log.info("-- end -- resizeDataVolume -- success");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- resizeDataVolume -- failed");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VOLUME_RESIZE_ERROR));
        }
    }

    /**
     * 删除 “云硬盘”
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DELETE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String delete(@Valid @RequestBody DeleteVolumeForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVolumeUuid());
        log.info("-- start -- deleteDataVolume");
        String result = volumeService.deleteDataVolume(f.getVolumeUuid());
        if (result.equals(ResponseMsgConstants.SUCCESS)) {
            log.info("-- end -- deleteDataVolume -- success");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- deleteDataVolume -- failed");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VOLUME_DELETE_ERROR));
        }
    }

    /**
     * 挂载 “云硬盘”
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.ATTACH, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String attach(@Valid @RequestBody AttachVolumeForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, LogUtils.formatUuidInjection(f.getVmUuid(), f.getVolumeUuid()));
        log.info("-- start -- attachDataVolume");
        String result = volumeService.attachDataVolume(f.getVmUuid(),f.getVolumeUuid());
        if (result.equals(ResponseMsgConstants.SUCCESS)) {
            log.info("-- end -- attachDataVolume -- success");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- attachDataVolume -- failed");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VOLUME_ATTACH_ERROR));
        }
    }

    /**
     * 卸载 “云硬盘”
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DETACH, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String detach(@Valid @RequestBody AttachVolumeForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, LogUtils.formatUuidInjection(f.getVmUuid(), f.getVolumeUuid()));
        log.info("-- start -- detach");
        String result = volumeService.detachDataVolume(f.getVmUuid(),f.getVolumeUuid());
        if (result.equals(ResponseMsgConstants.SUCCESS)) {
            log.info("-- end -- detach -- success");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- detach -- failed");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VOLUME_DETACH_ERROR));
        }
    }

    /**
     * 获取 “云硬盘” 列表
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("-- start -- queryAll");
        List<Volume> dataVolumeList = volumeService.queryAllDataVolume();
        log.info("-- end -- queryAll -- success");
        return JSON.toJSONString(BaseResponse.success(dataVolumeList));
    }

    /**
     * 分页获取 “云硬盘” 列表
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.PAGE_QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String pageQueryAll(
            @RequestParam(value = RequestParamConstants.PAGE_NUM) @NotNull @Min(1) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) @NotNull @Min(1) Integer pageSize) {
        log.info("-- start -- pageQueryAll");
        PageResponse<Volume> res = volumeService.pageQueryAllDataVolume(pageNum, pageSize);
        log.info("-- end -- pageQueryAll -- success");
        return JSON.toJSONString(BaseResponse.success(res));
    }

    /**
     * 分页模糊搜索获取 “云硬盘” 列表
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.FUZZY_QUERY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String fuzzyQuery(
            @RequestParam(value = RequestParamConstants.KEYWORDS) String keywords,
            @RequestParam(value = RequestParamConstants.STATUS) String status,
            @RequestParam(value = RequestParamConstants.PAGE_NUM) @NotNull @Min(1) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) @NotNull @Min(1) Integer pageSize) {
        log.info("-- start -- fuzzyQuery");
        PageResponse<Volume> res = volumeService.fuzzyQueryDataVolume(keywords, status, pageNum, pageSize);
        log.info("-- end -- fuzzyQuery -- success");
        return JSON.toJSONString(BaseResponse.success(res));
    }

    /**
     * 修改：名称、描述
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.EDIT, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String edit(@Valid @RequestBody EditVolumeForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVolumeUuid());
        log.info("-- start -- edit");
        Volume volume = volumeService.editVolume(f.getVolumeUuid(), f.getName(), f.getDescription(), f.getDiskSize());
        if (volume != null) {
            log.info("-- end -- edit -- success");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.success(volume));
        } else {
            log.warn("-- end -- edit -- failed");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VOLUME_RESIZE_ERROR));
        }
    }

    /**
     * 统计信息
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.STATISTICS, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String statistics() {
        log.info("-- start -- statistics");
        Map<String, Integer> resMap = volumeService.getStatistics();
        log.info("-- end -- statistics -- success");
        return JSON.toJSONString(BaseResponse.success(resMap));
    }
}
