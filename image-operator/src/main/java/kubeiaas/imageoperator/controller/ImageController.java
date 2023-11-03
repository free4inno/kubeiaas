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
        log.info("-- start -- imageQueryByUuid");
        Image image = imageService.queryByUuid(uuid);
        log.info("-- end -- imageQueryByUuid");
        return JSON.toJSONString(image);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_IMAGE_RAW_BY_UUID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String imageQueryRawByUuid(
            @RequestParam(value = RequestParamConstants.UUID) String uuid) {
        log.info("-- start -- imageQueryRawByUuid");
        String imageRaw = imageService.queryRawByUuid(uuid);
        log.info("-- end -- imageQueryRawByUuid");
        return imageRaw;
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_IMAGE_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String imageQueryAll() {
        log.info("-- start -- imageQueryAll");
        List<Image> imageList = imageService.queryAll();
        log.info("-- end -- imageQueryAll");
        return JSON.toJSONString(imageList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.PAGE_QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String imagePageQueryAll(
            @RequestParam(value = RequestParamConstants.PAGE_NUM) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) Integer pageSize) {
        log.info("-- start -- imagePageQueryAll");
        PageResponse<Image> res = imageService.pageQueryAll(pageNum, pageSize);
        log.info("-- end -- imagePageQueryAll");
        return JSON.toJSONString(res);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.FUZZY_QUERY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String imageFuzzyQuery(
            @RequestParam(value = RequestParamConstants.KEYWORDS) String keywords,
            @RequestParam(value = RequestParamConstants.PAGE_NUM) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) Integer pageSize) {
        log.info("-- start -- imageFuzzyQuery");
        PageResponse<Image> res = imageService.fuzzyQuery(keywords, pageNum, pageSize);
        log.info("-- end -- imageFuzzyQuery");
        return JSON.toJSONString(res);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.IMAGE_CREATE_YAML, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String imageCreateYaml(
            @RequestParam(value = RequestParamConstants.IMAGE_OBJECT) String imageObjectStr) {
        log.info("-- start -- imageCreateYaml");
        Image image = JSON.parseObject(imageObjectStr, Image.class);
        boolean res = imageService.imageCreateYaml(image);
        if (res) {
            log.info("-- end -- imageCreateYaml -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.info("-- end -- imageCreateYaml -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.IMAGE_SAVE_YAML, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String imageSaveYaml(@RequestBody SaveImageForm f) {
        log.info("-- start -- imageSaveYaml");
        boolean res = imageService.imageSaveYaml(f.getUuid(), f.getContent());
        if (res) {
            log.info("-- end -- imageSaveYaml -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.info("-- end -- imageSaveYaml -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String imageDelete(
            @RequestParam(value = RequestParamConstants.UUID) String uuid) {
        log.info("-- start -- imageDelete");
        boolean res = imageService.imageDelete(uuid);
        if (res) {
            log.info("-- end -- imageDelete -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.info("-- end -- imageDelete -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.STATISTICS, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public Integer statistics() {
        log.info("-- start -- statistics, get total num");
        Integer res = imageService.getTotalNum();
        log.info("-- end -- statistics");
        return res;
    }

}
