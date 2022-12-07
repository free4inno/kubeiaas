package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Host;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.request.host.SetHostRoleForm;
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

@Slf4j
@Validated
@Controller
@RequestMapping(value = RequestMappingConstants.HOST)
public class HostOpenAPI {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private HostService hostService;

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.SET_ROLE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String setRole(@Valid @RequestBody SetHostRoleForm f) throws BaseException {
        log.info("setRole ==== start ====");
        Host host = hostService.setRole(f.getHostUuid(), f.getRole());
        log.info("setRole ==== end ====");
        return JSON.toJSONString(BaseResponse.success(host));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("queryAll ==== start ====");
        List<Host> hostList = tableStorage.hostQueryAll();
        log.info("queryAll ==== end ====");
        return JSON.toJSONString(BaseResponse.success(hostList));
    }
}
