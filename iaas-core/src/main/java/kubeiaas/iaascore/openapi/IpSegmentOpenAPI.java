package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.IpSegment;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.request.IpSegment.CreateIpSegmentForm;
import kubeiaas.iaascore.request.IpSegment.EditIpSegmentForm;
import kubeiaas.iaascore.request.vm.CreateVmForm;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.ResponseEnum;
import kubeiaas.iaascore.response.SingleMsgResponse;
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
@RequestMapping(value = RequestMappingConstants.IP_SEGMENT)
public class IpSegmentOpenAPI {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private NetworkService networkService;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("ip_segment queryAll ==== start ====");
        List<IpSegment> ipSegmentList = networkService.queryAllIpSeg();
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

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.STATISTICS, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String statistics() {
        log.info("ip_segment statistics ==== start ====");
        Map<String, Integer> resMap = networkService.getIpCount();
        log.info("ip_segment statistics ==== end ====");
        return JSON.toJSONString(BaseResponse.success(resMap));
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.CREATE_IP_SEGMENT, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String createIpSegment(@Valid @RequestBody CreateIpSegmentForm f) throws BaseException {
        log.info("ip_segment crate ==== start ====");
        IpSegment newIpSegment = networkService.createIpSegment(f.getName(),f.getHostName(),f.getType(),f.getBridge(),f.getIpRangeStart(),f.getIpRangeEnd(),f.getGateway(),f.getNetmask());
        log.info("ip_segment crate ==== end ====");
        return JSON.toJSONString(BaseResponse.success(newIpSegment));
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DELETE_IP_SEGMENT, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String deleteIpSegment(
            @RequestParam(value = RequestParamConstants.IP_SEGMENT_ID) @NotNull Integer ipSegmentId) {
        log.info("ip_segment delete ==== start ====");
        String result  = networkService.deleteIpSegment(ipSegmentId);
        if (result.equals(ResponseMsgConstants.SUCCESS)) {
            log.info("delete ==== end ====");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.info("delete ==== error ====");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.WORK_ERROR));
        }
    }

    /**
     * 获取 ipSegment详情
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_BY_ID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryById(
            @RequestParam(value = RequestParamConstants.IP_SEGMENT_ID) @NotNull Integer ipSegmentId) {
        log.info("queryById ==== start ====");
        IpSegment ipSegment = networkService.queryById(ipSegmentId);
        log.info("queryById ==== end ====");
        return JSON.toJSONString(BaseResponse.success(ipSegment));
    }

    /**
     * 分页获取 ipSegment详情
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.PAGE_QUERY_BY_ID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String pageQueryById(
            @RequestParam(value = RequestParamConstants.IP_SEGMENT_ID) @NotNull Integer ipSegmentId,
            @RequestParam(value = RequestParamConstants.PAGE_NUM) @NotNull @Min(1) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) @NotNull @Min(1) Integer pageSize
            ) {
        log.info("pageQueryById ==== start ====");
        IpSegment ipSegment = networkService.pageQueryById(ipSegmentId,pageNum,pageSize);
        log.info("pageQueryById ==== end ====");
        return JSON.toJSONString(BaseResponse.success(ipSegment));
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.EDIT_IP_SEGMENT, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String EditIpSegment(@Valid @RequestBody EditIpSegmentForm f) throws BaseException {
        log.info("ip_segment edit ==== start ====");
        IpSegment newIpSegment = networkService.editIpSegment(f.getIpSegmentId(),f.getName(),f.getHostName(),f.getType(),f.getBridge(),f.getIpRangeStart(),f.getIpRangeEnd(),f.getGateway(),f.getNetmask());
        log.info("ip_segment edit ==== end ====");
        return JSON.toJSONString(BaseResponse.success(newIpSegment));
    }

}
