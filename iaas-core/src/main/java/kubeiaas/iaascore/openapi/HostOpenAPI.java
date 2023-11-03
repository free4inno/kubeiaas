package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Host;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.request.host.SetHostResourceForm;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.service.HostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@Controller
@RequestMapping(value = RequestMappingConstants.HOST)
public class HostOpenAPI {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private HostService hostService;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("-- start -- queryAll");

        log.info("-> invoke DB -- hostQueryAll");
        List<Host> hostList = tableStorage.hostQueryAll();
        log.info("<- invoke DB -- done");

        log.info("-- end -- queryAll -- success");
        return JSON.toJSONString(BaseResponse.success(hostList));
    }

    /**
     * 统计信息
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.STATISTICS, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String statistics() {
        log.info("-- start -- statistics");
        Map<String, Integer> resMap = hostService.getStatistics();
        log.info("-- end -- statistics -- success");
        return JSON.toJSONString(BaseResponse.success(resMap));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.NODE_RESOURCE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String nodeResource() {
        log.info("-- start -- nodeResource");
        Map<String, Object> resMap = hostService.getNodeResource();
        log.info("-- end -- nodeResource -- success");
        return JSON.toJSONString(BaseResponse.success(resMap));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.NET_STO_RESOURCE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String netStoResource() {
        log.info("-- start -- netStoResource");
        Map<String, Object> resMap = hostService.getNetStoResource();
        log.info("-- end -- netStoResource -- success");
        return JSON.toJSONString(BaseResponse.success(resMap));
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.NODE_RESOURCE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String setNodeResource(
            @Valid @RequestBody SetHostResourceForm f) throws BaseException {
        log.info("-- start -- setNodeResource");
        Host host = hostService.setResource(f.getHostUuid(), f.getVcpu(), f.getMem(), f.getStorage());
        log.info("-- end -- setNodeResource -- success");
        return JSON.toJSONString(BaseResponse.success(host));
    }
}
