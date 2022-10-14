package kubeiaas.iaascore.openapi;


import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Image;
import kubeiaas.common.bean.IpUsed;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
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

}
