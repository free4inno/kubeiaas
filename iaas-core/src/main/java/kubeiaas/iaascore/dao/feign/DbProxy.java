package kubeiaas.iaascore.dao.feign;

import kubeiaas.common.constants.ComponentConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@FeignClient(name = ComponentConstants.DB_PROXY, url = "http://127.0.0.1:9091")
public interface DbProxy {

    // ========================= vm =========================

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VM + "/" + RequestMappingConstants.QUERY_ALL)
    @ResponseBody
    String vmQueryAll();

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VM + "/" + RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY)
    @ResponseBody
    String vmQueryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1
    );

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VM + "/" + RequestMappingConstants.SAVE)
    @ResponseBody
    void vmSave(
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObjectStr
    );

    // ========================= image =========================

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.IMAGE + "/" + RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY)
    @ResponseBody
    String imageQueryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1
    );

    // ========================= host =========================

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.HOST + "/" + RequestMappingConstants.QUERY_ALL)
    @ResponseBody
    String hostQueryAll();

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.HOST + "/" + RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY)
    @ResponseBody
    String hostQueryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1
    );

    // ========================= ip segment =========================

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.IP_SEGMENT + "/" + RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY)
    @ResponseBody
    String ipSegmentQueryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1
    );

    // ========================= ip used =========================

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.IP_USED + "/" + RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY)
    @ResponseBody
    String ipUsedQueryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1
    );

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.IP_USED + "/" + RequestMappingConstants.SAVE)
    @ResponseBody
    void ipUsedSave(
            @RequestParam(value = RequestParamConstants.IP_USED_OBJECT) String ipUsedObjectStr
    );

}
