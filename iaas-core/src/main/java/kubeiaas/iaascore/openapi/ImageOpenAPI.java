package kubeiaas.iaascore.openapi;


import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Image;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.constraints.Min;
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

}
