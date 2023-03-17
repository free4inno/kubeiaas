package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Device;
import kubeiaas.common.bean.Host;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.ResponseEnum;
import kubeiaas.iaascore.scheduler.DeviceScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@Validated
@Controller
@RequestMapping(value = RequestMappingConstants.DEVICE)
public class DeviceOpenAPI {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private DeviceScheduler deviceScheduler;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_HOST_NAME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll(
            @RequestParam(value = RequestParamConstants.HOST_NAME) @NotEmpty @NotNull String hostName) throws BaseException {
        log.info("queryAll ==== start ====");
        Host host = tableStorage.hostQueryByName(hostName);
        if (null == host) {
            log.info("queryAll ==== error: host_name {} unknown.", hostName);
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.ARGS_ERROR));
        }
        List<Device> deviceList = deviceScheduler.queryAll(host);
        log.info("queryAll ==== end ====");
        return JSON.toJSONString(BaseResponse.success(deviceList));
    }
}
