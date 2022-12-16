package kubeiaas.iaascore.dao.feign;

import kubeiaas.common.constants.ComponentConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = ComponentConstants.IMAGE_OPERATOR, url = "http://image-operator:9093")
public interface ImageOperator {

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_IMAGE_BY_UUID)
    @ResponseBody
    String imageQueryByUuid(
            @RequestParam(value = RequestParamConstants.UUID) String uuid);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_IMAGE_ALL)
    @ResponseBody
    String imageQueryAll();

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.PAGE_QUERY_ALL)
    @ResponseBody
    String imagePageQueryAll(
            @RequestParam(value = RequestParamConstants.PAGE_NUM) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) Integer pageSize);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.FUZZY_QUERY)
    @ResponseBody
    String imageFuzzyQuery(
            @RequestParam(value = RequestParamConstants.KEYWORDS) String keywords,
            @RequestParam(value = RequestParamConstants.PAGE_NUM) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) Integer pageSize);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.IMAGE_CREATE_YAML)
    @ResponseBody
    String imageCreateYaml(
            @RequestParam(value = RequestParamConstants.IMAGE_OBJECT) String imageObjectStr);

}
