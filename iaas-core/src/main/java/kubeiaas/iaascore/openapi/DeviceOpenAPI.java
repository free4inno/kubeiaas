package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson2.JSON;
import kubeiaas.common.bean.Device;
import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.LogInjectionConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.enums.device.DeviceTypeEnum;
import kubeiaas.common.utils.EnumUtils;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.request.device.AttachDeviceForm;
import kubeiaas.iaascore.request.device.DetachDeviceForm;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.ResponseEnum;
import kubeiaas.iaascore.response.SingleMsgResponse;
import kubeiaas.iaascore.scheduler.DeviceScheduler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
        log.info("-- start -- queryAllByHostName");

        log.info(String.format("-> invoke DB -- hostQueryByName. hostName: %s", hostName));
        Host host = tableStorage.hostQueryByName(hostName);
        log.info("<- invoke DB -- done");
        if (null == host) {
            log.error(String.format("ERROR: host_name %s unknown.", hostName));
            throw new BaseException(
                    String.format("attach -- error: host_name %s unknown.", hostName), ResponseEnum.ARGS_ERROR);
        }

        log.info(String.format("-> invoke deviceScheduler -- queryAll. hostName: %s", hostName));
        List<Device> deviceList = deviceScheduler.queryAll(host);
        log.info("<- invoke deviceScheduler -- done");

        log.info("-- end -- queryAllByHostName -- success");
        return JSON.toJSONString(BaseResponse.success(deviceList));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_HOST_UUID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllByHostUuid(
            @RequestParam(value = RequestParamConstants.HOST_UUID) @NotEmpty @NotNull String hostUuid) throws BaseException {
        log.info("-- start -- queryAllByHostUuid");

        log.info(String.format("-> invoke DB -- hostQueryByUuid. hostUuid: %s", hostUuid));
        Host host = tableStorage.hostQueryByUuid(hostUuid);
        log.info("<- invoke DB -- done");
        if (null == host) {
            log.error(String.format("ERROR: host_uuid %s unknown.", hostUuid));
            throw new BaseException(
                    String.format("attach -- error: host_uuid %s unknown.", hostUuid), ResponseEnum.ARGS_ERROR);
        }

        log.info(String.format("-> invoke deviceScheduler -- queryAll. hostUuid: %s", hostUuid));
        List<Device> deviceList = deviceScheduler.queryAll(host);
        log.info("<- invoke deviceScheduler -- done");

        log.info("-- end -- queryAllByHostUuid -- success");
        return JSON.toJSONString(BaseResponse.success(deviceList));
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.ATTACH, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String attach(
            @Valid @RequestBody AttachDeviceForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVmUuid());
        log.info("-- start -- attach");

        // 1.1. get vm and check
        log.info("STEP 1: get virtual machine and host");

        log.info(String.format("-> invoke DB -- vmQueryByUuid. vmUuid: %s", f.getVmUuid()));
        Vm vm = tableStorage.vmQueryByUuid(f.getVmUuid());
        log.info("<- invoke DB -- done");
        if (null == vm) {
            log.error(String.format("ERROR: vm_uuid %s unknown.", f.getVmUuid()));
            throw new BaseException(
                    String.format("attach -- error: vm_uuid %s unknown.", f.getVmUuid()), ResponseEnum.ARGS_ERROR);
        }

        // 1.2. get host
        log.info(String.format("-> invoke DB -- hostQueryByUuid. hostUuid: %s", vm.getHostUuid()));
        Host host = tableStorage.hostQueryByUuid(vm.getHostUuid());
        log.info("<- invoke DB -- done");

        // 2. get device and check
        log.info("STEP 2: get device and check");
        DeviceTypeEnum deviceTypeEnum = EnumUtils.getEnumFromString(DeviceTypeEnum.class, f.getType());
        if (null == deviceTypeEnum) {
            log.error(String.format("ERROR: type %s unknown.", f.getType()));
            throw new BaseException(
                    String.format("attach -- error: type %s unknown.", f.getType()), ResponseEnum.ARGS_ERROR);
        }

        // 3. build a temp device for compare with list
        log.info("STEP 3: build a temp device for compare with list and attachment");
        Device tempDevice;
        try {
            tempDevice = new Device(deviceTypeEnum, f.getSign());
        } catch (Exception e) {
            log.error(String.format("ERROR: device sign %s parse failed.", f.getSign()));
            throw new BaseException(
                    String.format("attach -- error: device sign %s parse failed.", f.getSign()),
                    ResponseEnum.ARGS_ERROR);
        }

        log.info(String.format("-> invoke deviceScheduler -- attachDevice. hostUuid: %s, vmUuid: %s", host.getUuid(), vm.getUuid()));
        deviceScheduler.attachDevice(tempDevice, host, vm);
        log.info("<- invoke deviceScheduler -- done");

        // 4. response
        log.info("-- end -- attach -- success");

        // ----- clear MDC -----
        MDC.remove(LogInjectionConstants.UUID_INJECTION);
        return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DETACH, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String detach(
            @Valid @RequestBody DetachDeviceForm f) throws BaseException {
        MDC.put(LogInjectionConstants.UUID_INJECTION, f.getVmUuid());

        log.info("-- start -- detach");

        // 1.1. get vm and check
        log.info("STEP 1: get virtual machine and host");

        log.info(String.format("-> invoke DB -- vmQueryByUuid. vmUuid: %s", f.getVmUuid()));
        Vm vm = tableStorage.vmQueryByUuid(f.getVmUuid());
        log.info("<- invoke DB -- done");
        if (null == vm) {
            log.error(String.format("ERROR: vm_uuid %s unknown.", f.getVmUuid()));
            throw new BaseException(
                    String.format("detach -- error: vm_uuid %s unknown.", f.getVmUuid()),
                    ResponseEnum.ARGS_ERROR);
        }

        // 1.2. get host
        log.info(String.format("-> invoke DB -- hostQueryByUuid. hostUuid: %s", vm.getHostUuid()));
        Host host = tableStorage.hostQueryByUuid(vm.getHostUuid());
        log.info("<- invoke DB -- done");

        // 2. get device and check
        log.info("STEP 2: get device and check");
        DeviceTypeEnum deviceTypeEnum = EnumUtils.getEnumFromString(DeviceTypeEnum.class, f.getType());
        if (null == deviceTypeEnum) {
            String type = f.getType();
            log.error(String.format("ERROR: type %s unknown.", type));
            throw new BaseException(
                    String.format("detach -- error: type %s unknown.", type),
                    ResponseEnum.ARGS_ERROR);
        }

        // 3. build a temp device for compare with list
        log.info("STEP 3: build a temp device for compare with list and detach");
        Device tempDevice;
        try {
            tempDevice = new Device(deviceTypeEnum, f.getSign());
        } catch (Exception e) {
            log.error(String.format("ERROR: device sign %s parse failed.", f.getSign()));
            throw new BaseException(
                    String.format("detach -- error: device sign %s parse failed.", f.getSign()),
                    ResponseEnum.ARGS_ERROR);
        }

        log.info(String.format("-> invoke deviceScheduler -- detachDevice. hostUuid: %s, vmUuid: %s", host.getUuid(), vm.getUuid()));
        deviceScheduler.detachDevice(tempDevice, host, vm);
        log.info("<- invoke deviceScheduler -- done");

        // 4. return
        log.info("-- end -- detach -- success");

        // ----- clear MDC -----
        MDC.remove(LogInjectionConstants.UUID_INJECTION);
        return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
    }
}
