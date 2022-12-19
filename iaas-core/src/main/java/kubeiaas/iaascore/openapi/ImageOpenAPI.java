package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Image;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.request.image.DeleteImageForm;
import kubeiaas.iaascore.request.image.SaveImageForm;
import kubeiaas.iaascore.response.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

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
        log.info("image queryAll ==== start ====");
        List<Image> imageList = tableStorage.imageQueryAll();
        log.info("image queryAll ==== end ====");
        return JSON.toJSONString(BaseResponse.success(imageList));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.PAGE_QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String pageQueryAll(
            @RequestParam(value = RequestParamConstants.PAGE_NUM) @NotNull @Min(1) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) @NotNull @Min(1) Integer pageSize) {
        log.info("image pageQueryAll ==== start ====");
        PageResponse<Image> res = tableStorage.imagePageQueryAll(pageNum, pageSize);
        log.info("image pageQueryAll ==== end ====");
        return JSON.toJSONString(BaseResponse.success(res));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.FUZZY_QUERY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String fuzzyQuery(
            @RequestParam(value = RequestParamConstants.KEYWORDS) String keywords,
            @RequestParam(value = RequestParamConstants.PAGE_NUM) @NotNull @Min(1) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) @NotNull @Min(1) Integer pageSize) {
        log.info("image fuzzyQuery ==== start ====");
        PageResponse<Image> res = tableStorage.imageFuzzyQuery(keywords, pageNum, pageSize);
        log.info("image fuzzyQuery ==== end ====");
        return JSON.toJSONString(BaseResponse.success(res));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_IMAGE_RAW_BY_UUID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryRaw(
            @RequestParam(value = RequestParamConstants.UUID) @NotNull @NotEmpty String uuid) {
        log.info("image queryRaw ==== start ====");
        String content = tableStorage.imageGetRaw(uuid);
        SingleContentResponse res = new SingleContentResponse(content);
        log.info("image queryRaw ==== end ====");
        return JSON.toJSONString(BaseResponse.success(res));
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.SAVE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String save(@Valid @RequestBody SaveImageForm f) {
        log.info("image save ==== start ====");
        if (tableStorage.imageSave(f.getUuid(), f.getContent())) {
            log.info("image save ==== end ====");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.info("image save ==== error ====");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.IMAGE_SAVE_ERROR));
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DELETE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String delete(@Valid @RequestBody DeleteImageForm f) {
        log.info("image delete ==== start ====");
        if (tableStorage.imageDelete(f.getUuid())) {
            log.info("image delete ==== end ====");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.info("image delete ==== error ====");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.IMAGE_DELETE_ERROR));
        }
    }

}
