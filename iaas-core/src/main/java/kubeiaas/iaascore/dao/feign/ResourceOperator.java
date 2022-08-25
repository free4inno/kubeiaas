package kubeiaas.iaascore.dao.feign;

import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = "null", url = "http://127.0.0.1:9092")
public interface ResourceOperator {

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SELECT_HOST_BY_APPOINT)
    @ResponseBody
    String selectHostByAppoint(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid,
            @RequestParam(value = RequestParamConstants.HOST_UUID) String hostUuid);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SELECT_HOST_BY_OPERATOR)
    @ResponseBody
    String selectHostByOperator(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid,
            @RequestParam(value = RequestParamConstants.STRATEGY) String strategy);

}
