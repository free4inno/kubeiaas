package kubeiaas.iaascore.dao.feign;

import kubeiaas.common.constants.ComponentConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.iaascore.request.image.SaveImageForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

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

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_IMAGE_RAW_BY_UUID)
    @ResponseBody
    String imageQueryRawByUuid(
            @RequestParam(value = RequestParamConstants.UUID) String uuid);

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.IMAGE_SAVE_YAML)
    @ResponseBody
    String imageSaveYaml(@RequestBody SaveImageForm f);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE)
    @ResponseBody
    String imageDelete(
            @RequestParam(value = RequestParamConstants.UUID) String uuid);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.STATISTICS)
    @ResponseBody
    Integer statistics();
}
