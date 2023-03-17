package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Device;
import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.enums.device.DeviceTypeEnum;
import kubeiaas.common.utils.EnumUtils;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.request.device.AttachDeviceForm;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.ResponseEnum;
import kubeiaas.iaascore.response.SingleMsgResponse;
import kubeiaas.iaascore.scheduler.DeviceScheduler;
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
@RequestMapping(value = RequestMappingConstants.DEVICE)
public class DeviceOpenAPI {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private DeviceScheduler deviceScheduler;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_HOST_NAME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllByHostName(
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

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_HOST_UUID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllByHostUuid(
            @RequestParam(value = RequestParamConstants.HOST_UUID) @NotEmpty @NotNull String hostUuid) throws BaseException {
        log.info("queryAll ==== start ====");
        Host host = tableStorage.hostQueryByUuid(hostUuid);
        if (null == host) {
            log.info("queryAll ==== error: host_uuid {} unknown.", hostUuid);
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.ARGS_ERROR));
        }
        List<Device> deviceList = deviceScheduler.queryAll(host);
        log.info("queryAll ==== end ====");
        return JSON.toJSONString(BaseResponse.success(deviceList));
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.ATTACH, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String attach(
            @Valid @RequestBody AttachDeviceForm f) throws BaseException {
        log.info("attach ==== start ====");

        // 1. get host and check
        Host host = tableStorage.hostQueryByUuid(f.getHostUuid());
        if (null == host) {
            log.info("queryAll ==== error: host_uuid {} unknown.", f.getHostUuid());
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.ARGS_ERROR));
        }
        // 2. get vm and check
        Vm vm = tableStorage.vmQueryByUuid(f.getVmUuid());
        if (null == vm) {
            log.info("queryAll ==== error: vm_uuid {} unknown.", f.getVmUuid());
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.ARGS_ERROR));
        }
        // 3. get device and check
        DeviceTypeEnum deviceTypeEnum = EnumUtils.getEnumFromString(DeviceTypeEnum.class, f.getType());
        if (null == deviceTypeEnum) {
            log.info("queryAll ==== error: type {} unknown.", f.getType());
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.ARGS_ERROR));
        }

        // 3. build a temp device for compare with list
        Device tempDevice = new Device(null, deviceTypeEnum,
                f.getBus(), f.getDev(), f.getVendor(), f.getProduct(),
                null, null, null, null);
        boolean result = deviceScheduler.attachDevice(tempDevice, host, vm);

        // 4. response
        if (result) {
            log.info("attach ==== end ====");
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
        } else {
            log.info("attach ==== error ====");
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.DEVICE_ATTACH_ERROR));
        }
    }
}
