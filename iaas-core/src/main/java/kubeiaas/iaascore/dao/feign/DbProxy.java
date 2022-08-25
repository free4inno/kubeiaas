package kubeiaas.iaascore.dao.feign;

import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@FeignClient(name = "null", url = "http://127.0.0.1:9091")
public interface DbProxy {
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VM + "/" + RequestMappingConstants.QUERY_ALL)
    @ResponseBody
    String vmQueryAll();

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VM + "/" + RequestMappingConstants.SAVE)
    @ResponseBody
    void vmSave(
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObjectStr
    );

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.IMAGE + "/" + RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY)
    @ResponseBody
    String imageQueryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1
    );

}
