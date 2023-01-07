package kubeiaas.imageoperator.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Image;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.imageoperator.request.SaveImageForm;
import kubeiaas.imageoperator.response.PageResponse;
import kubeiaas.imageoperator.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Controller
public class ImageController {

    @Resource
    private ImageService imageService;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_IMAGE_BY_UUID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String imageQueryByUuid(
            @RequestParam(value = RequestParamConstants.UUID) String uuid) {
        log.info("imageQueryByUuid ==== start ====");
        Image image = imageService.queryByUuid(uuid);
        log.info("imageQueryByUuid ==== end ====");
        return JSON.toJSONString(image);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_IMAGE_RAW_BY_UUID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String imageQueryRawByUuid(
            @RequestParam(value = RequestParamConstants.UUID) String uuid) {
        log.info("imageQueryRawByUuid ==== start ====");
        String imageRaw = imageService.queryRawByUuid(uuid);
        log.info("imageQueryRawByUuid ==== end ====");
        return imageRaw;
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_IMAGE_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String imageQueryAll() {
        log.info("imageQueryAll ==== start ====");
        List<Image> imageList = imageService.queryAll();
        log.info("imageQueryAll ==== end ====");
        return JSON.toJSONString(imageList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.PAGE_QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String imagePageQueryAll(
            @RequestParam(value = RequestParamConstants.PAGE_NUM) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) Integer pageSize) {
        log.info("imagePageQueryAll ==== start ====");
        PageResponse<Image> res = imageService.pageQueryAll(pageNum, pageSize);
        log.info("imagePageQueryAll ==== end ====");
        return JSON.toJSONString(res);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.FUZZY_QUERY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String imageFuzzyQuery(
            @RequestParam(value = RequestParamConstants.KEYWORDS) String keywords,
            @RequestParam(value = RequestParamConstants.PAGE_NUM) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) Integer pageSize) {
        log.info("imageFuzzyQuery ==== start ====");
        PageResponse<Image> res = imageService.fuzzyQuery(keywords, pageNum, pageSize);
        log.info("imageFuzzyQuery ==== end ====");
        return JSON.toJSONString(res);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.IMAGE_CREATE_YAML, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String imageCreateYaml(
            @RequestParam(value = RequestParamConstants.IMAGE_OBJECT) String imageObjectStr) {
        log.info("imageCreateYaml ==== start ====");
        Image image = JSON.parseObject(imageObjectStr, Image.class);
        boolean res = imageService.imageCreateYaml(image);
        if (res) {
            log.info("imageCreateYaml ==== success ====");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.info("imageCreateYaml ==== failed ====");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.IMAGE_SAVE_YAML, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String imageSaveYaml(@RequestBody SaveImageForm f) {
        log.info("imageSaveYaml ==== start ====");
        boolean res = imageService.imageSaveYaml(f.getUuid(), f.getContent());
        if (res) {
            log.info("imageSaveYaml ==== success ====");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.info("imageSaveYaml ==== failed ====");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String imageDelete(
            @RequestParam(value = RequestParamConstants.UUID) String uuid) {
        log.info("imageDelete ==== start ====");
        boolean res = imageService.imageDelete(uuid);
        if (res) {
            log.info("imageDelete ==== success ====");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.info("imageDelete ==== failed ====");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.STATISTICS, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public Integer statistics() {
        log.info("statistics totalNum ==== start ====");
        Integer res = imageService.getTotalNum();
        log.info("statistics totalNum ==== end ====");
        return res;
    }

}
