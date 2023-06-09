package kubeiaas.iaasagent.dao.feign;

import kubeiaas.common.constants.ComponentConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = ComponentConstants.IMAGE_OPERATOR, url = "http://127.0.0.1:9089")
@RequestMapping(value = "image_operator")
public interface ImageOperator {

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_IMAGE_BY_UUID)
    @ResponseBody
    String imageQueryByUuid(
            @RequestParam(value = RequestParamConstants.UUID) String uuid);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_IMAGE_ALL)
    @ResponseBody
    String imageQueryAll();

}
