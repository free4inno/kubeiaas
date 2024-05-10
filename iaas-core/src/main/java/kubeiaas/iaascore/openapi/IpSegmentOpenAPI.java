package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson2.JSON;
import kubeiaas.common.bean.IpSegment;
import kubeiaas.common.bean.IpUsed;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.request.ipSegment.CreateIpSegmentForm;
import kubeiaas.iaascore.request.ipSegment.DeleteIpSegmentForm;
import kubeiaas.iaascore.request.ipSegment.EditIpSegmentForm;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.PageResponse;
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
        log.info("-- start -- queryAll");
        List<IpSegment> ipSegmentList = networkService.queryAllIpSeg();
        log.info("-- end -- queryAll -- success");
        return JSON.toJSONString(BaseResponse.success(ipSegmentList));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_HOST_AND_TYPE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllByHostAndType(
            @RequestParam(value = RequestParamConstants.HOST_UUID) @NotNull @NotEmpty String hostUuid,
            @RequestParam(value = RequestParamConstants.TYPE) @NotNull @NotEmpty String type) {
        log.info("-- start -- queryAllByHostAndType");

        log.info(String.format("-> invoke DB -- ipSegmentQueryAllByHostAndType. hostUuid: %s, type: %s", hostUuid, type));
        List<IpSegment> ipSegmentList = tableStorage.ipSegmentQueryAllByHostAndType(hostUuid, type);
        log.info("<- invoke DB -- done");

        log.info("-- end -- queryAllByHostAndType -- success");
        return JSON.toJSONString(BaseResponse.success(ipSegmentList));
    }

    /**
     * 统计信息
     * @return 四字段公私分类Map
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.STATISTICS, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String statistics() {
        log.info("-- start -- statistics");
        Map<String, Integer> resMap = networkService.getIpCount();
        log.info("-- end -- statistics -- success");
        return JSON.toJSONString(BaseResponse.success(resMap));
    }

    /**
     * ip用量统计
     * @param ipSegmentId id
     * @return 两字段用量统计Map
     * @throws BaseException id异常
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.STATISTICS_BY_ID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String statisticsById(
            @RequestParam(value = RequestParamConstants.IP_SEGMENT_ID) Integer ipSegmentId) throws BaseException {
        log.info("-- start -- statisticsById");
        Map<String, Integer> resMap;
        if (ipSegmentId == null) {
            log.error("ERROR: ipSegmentId illegal");
            throw new BaseException("err: ipSegmentId illegal");
        } else {
            resMap = networkService.getIpCount(ipSegmentId);
        }
        log.info("-- end -- statisticsById -- success");
        return JSON.toJSONString(BaseResponse.success(resMap));
    }

    /**
     * 新建网段
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.CREATE_IP_SEGMENT, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String createIpSegment(@Valid @RequestBody CreateIpSegmentForm f) throws BaseException {
        log.info("-- start -- createIpSegment");
        IpSegment newIpSegment = networkService.updateIpSegment(0,
                f.getName(), f.getHostUuid(), f.getType(), f.getBridge(), f.getIpRangeStart(), f.getIpRangeEnd(), f.getGateway(), f.getNetmask(), true);
        log.info("-- end -- createIpSegment -- success");
        return JSON.toJSONString(BaseResponse.success(newIpSegment));
    }

    /**
     * 删除网段
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DELETE_IP_SEGMENT, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String deleteIpSegment(@Valid @RequestBody DeleteIpSegmentForm f) {
        log.info("-- start -- deleteIpSegment");
        String result  = networkService.deleteIpSegment(f.getIpSegmentId());
        if (result.equals(ResponseMsgConstants.SUCCESS)) {
            log.info("-- end -- deleteIpSegment -- success");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.warn("-- end -- deleteIpSegment -- failed");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.WORK_ERROR));
        }
    }

    /**
     * 编辑网段
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.EDIT_IP_SEGMENT, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String editIpSegment(@Valid @RequestBody EditIpSegmentForm f) throws BaseException {
        log.info("-- start -- editIpSegment");
        IpSegment newIpSegment = networkService.updateIpSegment(f.getIpSegmentId(),
                f.getName(), f.getHostUuid(), f.getType(), f.getBridge(), f.getIpRangeStart(), f.getIpRangeEnd(), f.getGateway(), f.getNetmask(), false);
        log.info("-- end -- editIpSegment -- success");
        return JSON.toJSONString(BaseResponse.success(newIpSegment));
    }

    /**
     * 获取 ipSegment 详情
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_BY_ID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryById(
            @RequestParam(value = RequestParamConstants.IP_SEGMENT_ID) @NotNull Integer ipSegmentId) {
        log.info("-- start -- queryById");
        IpSegment ipSegment = networkService.queryById(ipSegmentId);
        log.info("-- end -- queryById -- success");
        return JSON.toJSONString(BaseResponse.success(ipSegment));
    }

    /**
     * 获取 ipSegment 段内 IP 分页详情
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.PAGE_QUERY_BY_ID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String pageQueryIpsById(
            @RequestParam(value = RequestParamConstants.IP_SEGMENT_ID) @NotNull Integer ipSegmentId,
            @RequestParam(value = RequestParamConstants.PAGE_NUM) @NotNull @Min(1) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) @NotNull @Min(1) Integer pageSize) {
        log.info("-- start -- pageQueryById");
        PageResponse<IpUsed> res = networkService.pageQueryById(ipSegmentId, pageNum, pageSize);
        log.info("-- end -- pageQueryById -- success");
        return JSON.toJSONString(BaseResponse.success(res));
    }

}
