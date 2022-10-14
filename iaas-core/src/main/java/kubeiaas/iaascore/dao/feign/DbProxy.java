package kubeiaas.iaascore.dao.feign;

import kubeiaas.common.constants.ComponentConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@FeignClient(name = ComponentConstants.DB_PROXY, url = "http://db-proxy:9091")
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
    String vmSave(
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObjectStr
    );

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VM + "/" + RequestMappingConstants.UPDATE)
    @ResponseBody
    String updateVm(
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObjectStr
    );

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VM + "/" + RequestMappingConstants.DELETE_BY_UUID)
    void vmDeleteByUuid(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid
    );

    // ========================= image =========================

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.IMAGE + "/" + RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY)
    @ResponseBody
    String imageQueryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1
    );

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.IMAGE + "/" + RequestMappingConstants.QUERY_ALL)
    @ResponseBody
    String imageQueryAll();

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

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.HOST + "/" + RequestMappingConstants.QUERY_ALL_LIKE_BY_SINGLE_KEY)
    @ResponseBody
    String hostQueryAllLikeBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1
    );

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.HOST + "/" + RequestMappingConstants.SAVE)
    @ResponseBody
    String hostSave(
            @RequestParam(value = RequestParamConstants.HOST_OBJECT) String hostObjectStr
    );

    // ========================= ip segment =========================

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.IP_SEGMENT + "/" + RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY)
    @ResponseBody
    String ipSegmentQueryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1
    );

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.IP_SEGMENT + "/" + RequestMappingConstants.QUERY_ALL)
    @ResponseBody
    String ipSegmentQueryAll();

    // ========================= ip used =========================

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.IP_USED + "/" + RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY)
    @ResponseBody
    String ipUsedQueryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1
    );

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.IP_USED + "/" + RequestMappingConstants.SAVE)
    @ResponseBody
    String ipUsedSave(
            @RequestParam(value = RequestParamConstants.IP_USED_OBJECT) String ipUsedObjectStr
    );

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.IP_USED + "/" + RequestMappingConstants.DELETE_ALL_BY_UUID)
    @ResponseBody
    String ipUsedDeleteByVmUuid(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid
    );

    // ========================= volume =========================
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VOLUME + "/" + RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY)
    @ResponseBody
    String volumeQueryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1
    );

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VOLUME + "/" + RequestMappingConstants.SAVE)
    @ResponseBody
    String volumeSave(
            @RequestParam(value = RequestParamConstants.VOLUME_OBJECT) String volumeObjectStr
    );

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.VOLUME + "/" + RequestMappingConstants.DELETE_BY_UUID)
    @ResponseBody
    String volumeDeleteByUuid(
            @RequestParam(value = RequestParamConstants.VOLUME_UUID) String volumeUuid
    );
}
