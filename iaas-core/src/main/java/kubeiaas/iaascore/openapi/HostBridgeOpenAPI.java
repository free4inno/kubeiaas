package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson2.JSON;
import kubeiaas.common.bean.HostBridge;
import kubeiaas.common.bean.IpSegment;
import kubeiaas.common.bean.IpUsed;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.request.hostBridge.CreateHostBridgeForm;
import kubeiaas.iaascore.request.ipSegment.CreateIpSegmentForm;
import kubeiaas.iaascore.request.ipSegment.DeleteIpSegmentForm;
import kubeiaas.iaascore.request.ipSegment.EditIpSegmentForm;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.PageResponse;
import kubeiaas.iaascore.response.ResponseEnum;
import kubeiaas.iaascore.response.SingleMsgResponse;
import kubeiaas.iaascore.service.HostBridgeService;
import kubeiaas.iaascore.service.NetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@Controller
@RequestMapping(value = RequestMappingConstants.HOST_BRIDGE)
public class HostBridgeOpenAPI {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private HostBridgeService hostBridgeService;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("-- start -- queryAll");
        List<HostBridge> hostBridgeList = hostBridgeService.queryAllHostBridge();
        log.info("-- end -- queryAll -- success");
        return JSON.toJSONString(BaseResponse.success(hostBridgeList));
    }


    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_IP_SEGMENT_ID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllByIpSegmentId(
            @RequestParam(value = RequestParamConstants.IP_SEGMENT_ID) @NotNull Integer ipSegmentId) {
        log.info("-- start -- queryByIpSegmentId");
        List<HostBridge> hostBridgeList = hostBridgeService.queryAllByIpSegmentId(ipSegmentId);
        log.info("-- end -- queryByIpSegmentId -- success");
        return JSON.toJSONString(BaseResponse.success(hostBridgeList));
    }

//    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.MIGRATE_FROM_IP_SEGMENT, produces = RequestMappingConstants.APP_JSON)
//    @ResponseBody
//    public String migrateFromIpSegment() {
//        log.info("-- start -- migrateFromOldIpSegment");
//        String result  = hostBridgeService.migrateFromIpSegment();
//        if (result.equals(ResponseMsgConstants.SUCCESS)) {
//            log.info("-- end -- migrateFromOldIpSegment -- success");
//            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
//        } else {
//            log.warn("-- end -- migrateFromOldIpSegment -- failed");
//            return JSON.toJSONString(BaseResponse.error(ResponseEnum.WORK_ERROR));
//        }
//    }

    /**
     * 新建HostBridge
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.CREATE_HOST_BRIDGE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String createHostBridge(@Valid @RequestBody CreateHostBridgeForm f) throws BaseException {
        log.info("-- start -- createHostBridge");
        HostBridge newHostBridge = hostBridgeService.updateHostBridge(0,
                f.getBridgeName(), Integer.parseInt(f.getIpSegmentId()), f.getHostName(), true);
        log.info("-- end -- createHostBridge -- success");
        return JSON.toJSONString(BaseResponse.success(newHostBridge));
    }

    /**
     * 删除HostBridge
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_HOST_BRIDGE_BY_ID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String deleteHostBridgeById(
            @RequestParam(value = RequestParamConstants.HOST_BRIDGE_ID) @NotNull Integer hostBridgeId) {
        log.info("-- start -- deleteHostBridge");
        String result  = hostBridgeService.deleteHostBridgeById(hostBridgeId);
        if (result.equals(ResponseMsgConstants.SUCCESS)) {
            log.info("-- end -- deleteHostBridge -- success");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- deleteHostBridge -- failed");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.WORK_ERROR));
        }
    }

}
