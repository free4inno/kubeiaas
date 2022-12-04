package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.VolumeException;
import kubeiaas.iaascore.request.volume.AttachVolumeForm;
import kubeiaas.iaascore.request.volume.CreateVolumeForm;
import kubeiaas.iaascore.request.volume.DeleteVolumeForm;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.ResponseEnum;
import kubeiaas.iaascore.response.SingleMsgResponse;
import kubeiaas.iaascore.response.VolumePageResponse;
import kubeiaas.iaascore.service.VolumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;


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
        log.info("create ==== start ====");
        Volume newVolume = volumeService.createDataVolume(f.getName(), f.getDiskSize(), f.getDescription(), f.getHostUuid());
        log.info("create ==== end ====");
        return JSON.toJSONString(BaseResponse.success(newVolume));
    }

    /**
     * 删除 “云硬盘”
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DELETE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String delete(@Valid @RequestBody DeleteVolumeForm f) throws BaseException {
        log.info("delete ==== start ====");
        String result = volumeService.deleteDataVolume(f.getVolumeUuid());
        if (result.equals(ResponseMsgConstants.SUCCESS)) {
            log.info("delete ==== end ====");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.info("delete ==== error ====");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VOLUME_DELETE_ERROR));
        }
    }

    /**
     * 挂载 “云硬盘”
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.ATTACH, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String attach(@Valid @RequestBody AttachVolumeForm f) throws BaseException {
        log.info("attach ==== start ====");
        String result = volumeService.attachDataVolume(f.getVmUuid(),f.getVolumeUuid());
        if (result.equals(ResponseMsgConstants.SUCCESS)) {
            log.info("attach ==== end ====");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.info("attach ==== error ====");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VOLUME_ATTACH_ERROR));
        }
    }

    /**
     * 卸载 “云硬盘”
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DETACH, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String detach(@Valid @RequestBody AttachVolumeForm f) throws BaseException {
        log.info("detach ==== start ====");
        String result = volumeService.detachDataVolume(f.getVmUuid(),f.getVolumeUuid());
        if (result.equals(ResponseMsgConstants.SUCCESS)) {
            log.info("detach ==== end ====");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.info("detach ==== success ====");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VOLUME_DETACH_ERROR));
        }
    }

    /**
     * 获取 “云硬盘” 列表
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("queryAll ==== start ====");
        List<Volume> dataVolumeList = volumeService.queryAllDataVolume();
        log.info("queryAll ==== end ====");
        return JSON.toJSONString(BaseResponse.success(dataVolumeList));
    }

    /**
     * 模糊查询获取 “云硬盘” 列表
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.FUZZY_QUERY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String fuzzyQuery(@RequestParam(value = RequestParamConstants.KEYWORDS)  String keywords,
                             @RequestParam(value = RequestParamConstants.STATUS) String status) {
        log.info("fuzzyQuery ==== start ====");
        List<Volume> dataVolumeList = volumeService.FuzzyQueryDataVolume(keywords, status);
        log.info("fuzzyQuery ==== end ====");
        return JSON.toJSONString(BaseResponse.success(dataVolumeList));
    }

    /**
     * 分页模糊搜索获取 “云硬盘” 列表
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.PAGE_FUZZY_QUERY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String pageFuzzyQuery(
            @RequestParam(value = RequestParamConstants.KEYWORDS)  String keywords,
            @RequestParam(value = RequestParamConstants.STATUS) String status,
            @RequestParam(value = RequestParamConstants.PAGE_NUM) @NotNull @Min(1) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) @NotNull @Min(1) Integer pageSize) {
        log.info("pageFuzzyQuery ==== start ====");
        VolumePageResponse res = volumeService.pageFuzzyQueryDataVolume(keywords, status, pageNum, pageSize);
        log.info("pageFuzzyQuery ==== end ====");
        return JSON.toJSONString(BaseResponse.success(res));
    }

    /**
     * 分页获取 “云硬盘” 列表
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.PAGE_QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String pageQueryAll(
            @RequestParam(value = RequestParamConstants.PAGE_NUM) @NotNull @Min(1) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) @NotNull @Min(1) Integer pageSize) {
        log.info("pageQueryAll ==== start ====");
        VolumePageResponse res = volumeService.pageQueryAllDataVolume(pageNum, pageSize);
        log.info("pageQueryAll ==== end ====");
        return JSON.toJSONString(BaseResponse.success(res));
    }

    /**
     * 分页模糊搜索获取 “云硬盘” 列表
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.FUZZY_QUERY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String fuzzyQuery(
            @RequestParam(value = RequestParamConstants.KEYWORDS)  String keywords,
            @RequestParam(value = RequestParamConstants.STATUS) String status,
            @RequestParam(value = RequestParamConstants.PAGE_NUM) @NotNull @Min(1) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) @NotNull @Min(1) Integer pageSize) {
        log.info("fuzzyQuery ==== start ====");
        VolumePageResponse res = volumeService.fuzzyQueryDataVolume(keywords, status, pageNum, pageSize);
        log.info("fuzzyQuery ==== end ====");
        return JSON.toJSONString(BaseResponse.success(res));
    }
}
