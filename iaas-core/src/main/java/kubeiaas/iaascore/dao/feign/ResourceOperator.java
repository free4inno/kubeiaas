package kubeiaas.iaascore.dao.feign;

import kubeiaas.common.constants.ComponentConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = ComponentConstants.RESOURCE_OPERATOR, url = "http://resource-operator:9092")
public interface ResourceOperator {

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SELECT_HOST_BY_APPOINT)
    @ResponseBody
    String selectHostByAppoint(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid,
            @RequestParam(value = RequestParamConstants.HOST_UUID) String hostUuid);


    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SELECT_HOST_BY_HOST_UUID)
    @ResponseBody
    String selectHostByHostUuid(
            @RequestParam(value = RequestParamConstants.HOST_UUID) String hostUuid);


    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SELECT_HOST_BY_OPERATOR)
    @ResponseBody
    String selectHostByOperator(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid,
            @RequestParam(value = RequestParamConstants.STRATEGY) String strategy);


    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SELECT_HOST_BY_HOST_OPERATOR)
    @ResponseBody
    String selectHostByHostOperator(
            @RequestParam(value = RequestParamConstants.STRATEGY) String strategy);


}
