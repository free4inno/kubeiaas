package kubeiaas.resourceoperator.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Host;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.resourceoperator.service.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Slf4j
@Controller
public class ResourceController {

    @Resource
    private ResourceService resourceService;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SELECT_HOST_BY_APPOINT, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String selectHostByAppoint(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid,
            @RequestParam(value = RequestParamConstants.HOST_UUID) String hostUuid) {
        log.info("selectHostByAppoint ==== start ====");
        Host host = resourceService.selectHostByAppoint(vmUuid, hostUuid);
        log.info("selectHostByAppoint ==== end ====");
        return JSON.toJSONString(host);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SELECT_HOST_BY_HOST_UUID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String selectHostByHostUuid(
            @RequestParam(value = RequestParamConstants.HOST_UUID) String hostUuid) {
        log.info("selectHostByAppoint ==== start ====");
        Host host = resourceService.selectHostByHostUuid(hostUuid);
        log.info("selectHostByAppoint ==== end ====");
        return JSON.toJSONString(host);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SELECT_HOST_BY_OPERATOR, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String selectHostByOperator(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid,
            @RequestParam(value = RequestParamConstants.STRATEGY) String strategy) {
        log.info("selectHostByOperator ==== start ====");
        Host host = resourceService.selectHostByStrategy(vmUuid, strategy);
        log.info("selectHostByOperator ==== end ====");
        return JSON.toJSONString(host);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SELECT_HOST_BY_HOST_OPERATOR, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String selectHostByHostOperator(
            @RequestParam(value = RequestParamConstants.STRATEGY) String strategy) {
        log.info("selectHostByOperator ==== start ====");
        Host host = resourceService.selectHostByStrategy(strategy);
        log.info("selectHostByOperator ==== end ====");
        return JSON.toJSONString(host);
    }

}
