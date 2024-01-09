package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Image;
import kubeiaas.common.constants.LogInjectionConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.ImageConstants;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.request.image.DeleteImageForm;
import kubeiaas.iaascore.request.image.SaveImageForm;
import kubeiaas.iaascore.request.image.UploadImageForm;
import kubeiaas.iaascore.response.*;
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
@RequestMapping(value = RequestMappingConstants.IMAGE)
public class ImageOpenAPI {

    @Resource
    private TableStorage tableStorage;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("-- start -- queryAll");

        log.info("-> invoke DB -- imageQueryAll");
        List<Image> imageList = tableStorage.imageQueryAll();
        log.info("<- invoke DB -- done");

        log.info("-- end -- queryAll -- success");
        return JSON.toJSONString(BaseResponse.success(imageList));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.PAGE_QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String pageQueryAll(
            @RequestParam(value = RequestParamConstants.PAGE_NUM) @NotNull @Min(1) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) @NotNull @Min(1) Integer pageSize) {
        log.info("-- start -- pageQueryAll");

        log.info(String.format("-> invoke DB -- imagePageQueryAll. pageNum: %s, pageSize: %s", pageNum, pageSize));
        PageResponse<Image> res = tableStorage.imagePageQueryAll(pageNum, pageSize);
        log.info("<- invoke DB -- done");

        log.info("-- end -- pageQueryAll -- success");
        return JSON.toJSONString(BaseResponse.success(res));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.FUZZY_QUERY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String fuzzyQuery(
            @RequestParam(value = RequestParamConstants.KEYWORDS) String keywords,
            @RequestParam(value = RequestParamConstants.PAGE_NUM) @NotNull @Min(1) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) @NotNull @Min(1) Integer pageSize) {
        log.info("-- start -- fuzzyQuery");

        log.info(String.format("-> invoke DB -- imageFuzzyQuery. keywords: %s, pageNum: %s, pageSize: %s", keywords, pageNum, pageSize));
        PageResponse<Image> res = tableStorage.imageFuzzyQuery(keywords, pageNum, pageSize);
        log.info("<- invoke DB -- done");

        log.info("-- end -- fuzzyQuery -- success");
        return JSON.toJSONString(BaseResponse.success(res));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_IMAGE_RAW_BY_UUID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryRaw(
            @RequestParam(value = RequestParamConstants.UUID) @NotNull @NotEmpty String uuid) {
        MDC.put(LogInjectionConstants.UUID_INJECTION, uuid);
        log.info("-- start -- queryRaw");

        log.info(String.format("-> invoke DB -- imageGetRaw. uuid: %s", uuid));
        String content = tableStorage.imageGetRaw(uuid);
        log.info("<- invoke DB -- done");

        SingleContentResponse res = new SingleContentResponse(content);
        log.info("-- end -- queryRaw -- success");
        MDC.remove(LogInjectionConstants.UUID_INJECTION);
        return JSON.toJSONString(BaseResponse.success(res));
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.SAVE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String save(@Valid @RequestBody SaveImageForm f) {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getUuid());
        log.info("-- start -- save");

        log.info(String.format("-> invoke DB -- imageSave. uuid: %s, content: %s", f.getUuid(), f.getContent()));
        boolean status = tableStorage.imageSave(f.getUuid(), f.getContent());
        log.info("<- invoke DB -- done");
        if (status) {
            log.info("-- end -- save -- success");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- save -- failed");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.IMAGE_SAVE_ERROR));
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.UPLOAD, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String upload(@Valid @RequestBody UploadImageForm f) {
        log.info("-- start -- upload");
        log.info(String.format("-> invoke DB -- imageUpload. id: %s", f.getId()));
        boolean status = tableStorage.imageUpload(f.getId());
        log.info("<- invoke DB -- done");

        if (status) {
            log.info("-- end -- upload -- success");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- upload -- failed");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.IMAGE_UPLOAD_ERROR));
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DELETE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String delete(@Valid @RequestBody DeleteImageForm f) {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getUuid());
        log.info("-- start -- delete");

        log.info(String.format("-> invoke DB -- imageDelete. uuid: %s", f.getUuid()));
        boolean status = tableStorage.imageDelete(f.getUuid());
        log.info("<- invoke DB -- done");
        if (status) {
            log.info("-- end -- delete -- success");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- delete -- failed");
            MDC.remove(LogInjectionConstants.UUID_INJECTION);
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.IMAGE_DELETE_ERROR));
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.STATISTICS, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String statistics() {
        log.info("-- start -- statistics");
        Map<String, Integer> resMap = new HashMap<>();

        log.info("-> invoke DB -- imageTotalNum");
        Integer totalNum = tableStorage.imageTotalNum();
        log.info("<- invoke DB -- done");

        resMap.put(ImageConstants.TOTAL, totalNum);
        log.info("-- end -- statistics -- success");
        return JSON.toJSONString(BaseResponse.success(resMap));
    }

}
