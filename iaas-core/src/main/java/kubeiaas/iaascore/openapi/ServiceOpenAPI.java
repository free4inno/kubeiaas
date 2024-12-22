package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson2.JSON;
import kubeiaas.common.constants.ComponentConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.enums.service.ServiceStatusEnum;
import kubeiaas.iaascore.config.ServiceConfig;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.SingleMsgResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@Controller
@RequestMapping(value = RequestMappingConstants.SERVICE)
public class ServiceOpenAPI {

    @Resource
    private ServiceConfig serviceConfig;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_SERVICE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryService() {
        log.info("-- start -- queryService");
        Map<String, List<String>> resMap = serviceConfig.getSvc();
        log.info("-- end -- queryService -- success");
        return JSON.toJSONString(BaseResponse.success(resMap));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.REGISTER, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String register(
            @RequestParam(value = RequestParamConstants.SERVICE_NAME) @NotNull @NotEmpty String serviceName,
            @RequestParam(value = RequestParamConstants.NODE_NAME) @NotNull @NotEmpty String nodeName,
            @RequestParam(value = RequestParamConstants.TIMESTAMP) @NotNull Long timestamp) {
        log.debug("-- start -- register");
        serviceConfig.register(serviceName, nodeName, timestamp);
        log.debug("-- end -- register -- success");
        return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_STORAGE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryStorage() {
        log.info("-- start -- queryStorage");
        Map<String, Object> resMap = new HashMap<>();
        resMap.put(ComponentConstants.NFS, serviceConfig.getNfs());
        log.info("-- end -- queryStorage -- success");
        return JSON.toJSONString(BaseResponse.success(resMap));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.STATISTICS, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String statistics() {
        log.info("-- start -- statistics");
        Map<String, Integer> resMap = serviceConfig.getSvcCount();
        log.info("-- end -- statistics -- success");
        return JSON.toJSONString(BaseResponse.success(resMap));
    }
}
