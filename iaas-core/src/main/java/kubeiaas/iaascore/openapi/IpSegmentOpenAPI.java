package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.IpSegment;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.response.BaseResponse;
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
@RequestMapping(value = RequestMappingConstants.IP_SEGMENT)
public class IpSegmentOpenAPI {

    @Resource
    private TableStorage tableStorage;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("ip_segment queryAll ==== start ====");
        List<IpSegment> ipSegmentList = tableStorage.ipSegmentQueryAll();
        log.info("ip_segment queryAll ==== end ====");
        return JSON.toJSONString(BaseResponse.success(ipSegmentList));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_HOST_AND_TYPE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllByHostAndType(
            @RequestParam(value = RequestParamConstants.HOST_UUID) @NotNull @NotEmpty String hostUuid,
            @RequestParam(value = RequestParamConstants.TYPE) @NotNull @NotEmpty String type) {
        log.info("ip_segment queryAllByHostAndType ==== start ====");
        List<IpSegment> ipSegmentList = tableStorage.ipSegmentQueryAllByHostAndType(hostUuid, type);
        log.info("ip_segment queryAllByHostAndType ==== end ====");
        return JSON.toJSONString(BaseResponse.success(ipSegmentList));
    }
}
