package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.SpecConfig;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.enums.config.SpecTypeEnum;
import kubeiaas.common.utils.EnumUtils;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.request.specConfig.CreateSpecConfigForm;
import kubeiaas.iaascore.request.specConfig.DeleteSpecConfigForm;
import kubeiaas.iaascore.request.specConfig.EditSpecConfigForm;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.ResponseEnum;
import kubeiaas.iaascore.response.SingleMsgResponse;
import kubeiaas.iaascore.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@Validated
@Controller
@RequestMapping(value = RequestMappingConstants.SPEC_CONFIG)
public class SpecConfigOpenAPI {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private ConfigService configService;

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

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("queryAll ==== start ====");
        // 1. call request
        List<SpecConfig> specConfigList = tableStorage.specConfigQueryAll();
        log.info("queryAll ==== end ====");
        return JSON.toJSONString(BaseResponse.success(specConfigList));
    }

    /**
     * 新建
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.CREATE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String create(@Valid @RequestBody CreateSpecConfigForm f) throws BaseException {
        log.info("create ==== start ====");
        SpecConfig specConfig = configService.specConfigSave(null, f.getType(), f.getValue(), f.getDescription());
        log.info("create ==== end ====");
        return JSON.toJSONString(BaseResponse.success(specConfig));
    }

    /**
     * 删除
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DELETE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String delete(@Valid @RequestBody DeleteSpecConfigForm f) throws BaseException {
        log.info("delete == id: " + f.getId());
        configService.specConfigDelete(f.getId());
        return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
    }

    /**
     * 编辑
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.EDIT, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String edit(@Valid @RequestBody EditSpecConfigForm f) throws BaseException {
        log.info("edit ==== start ====");
        SpecConfig specConfig = configService.specConfigSave(f.getId(), f.getType(), f.getValue(), f.getDescription());
        log.info("edit ==== end ====");
        return JSON.toJSONString(BaseResponse.success(specConfig));
    }
}
