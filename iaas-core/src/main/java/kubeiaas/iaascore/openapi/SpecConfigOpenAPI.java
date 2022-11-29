package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.SpecConfig;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.enums.config.SpecTypeEnum;
import kubeiaas.common.utils.EnumUtils;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.SPEC_CONFIG)
public class SpecConfigOpenAPI {

    @Resource
    private TableStorage tableStorage;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_TYPE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllByType(
            @RequestParam(value = RequestParamConstants.TYPE) @NotNull @NotEmpty String type) {
        log.info("queryAllByType ==== start ====");
        // 1. check Enum param
        SpecTypeEnum specTypeEnum = EnumUtils.getEnumFromString(SpecTypeEnum.class, type);
        if (specTypeEnum == null) {
            log.info("queryAllByType ==== error ====");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.ARGS_ERROR));
        }
        // 2. call request
        List<SpecConfig> specConfigList = tableStorage.specConfigQueryAllByType(specTypeEnum);
        log.info("queryAllByType ==== end ====");
        return JSON.toJSONString(BaseResponse.success(specConfigList));
    }
}
