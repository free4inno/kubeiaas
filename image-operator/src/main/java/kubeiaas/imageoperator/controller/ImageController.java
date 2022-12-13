package kubeiaas.imageoperator.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Image;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.imageoperator.response.PageResponse;
import kubeiaas.imageoperator.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

}
