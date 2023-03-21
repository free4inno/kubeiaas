package kubeiaas.iaasagent.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Device;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.iaasagent.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.DEVICE_C)
public class DeviceController {

    @Resource
    private DeviceService deviceService;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("queryAll ==== ");
        List<Device> deviceList = deviceService.queryAll();
        return JSON.toJSONString(deviceList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.ATTACH, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String attach(
            @RequestParam(value = RequestParamConstants.DEVICE_OBJECT) String deviceObjectStr,
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObject
    ) {
        log.info("attach ==== start");
        Vm vm = JSON.parseObject(vmObject, Vm.class);
        Device device = JSON.parseObject(deviceObjectStr, Device.class);
        if (deviceService.attach(device, vm)) {
            log.info("attach -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.info("attach -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DETACH, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String detach(
            @RequestParam(value = RequestParamConstants.DEVICE_OBJECT) String deviceObjectStr,
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObject
    ) {
        log.info("detach ==== start");
        Vm vm = JSON.parseObject(vmObject, Vm.class);
        Device device = JSON.parseObject(deviceObjectStr, Device.class);
        if (deviceService.detach(device, vm)) {
            log.info("detach -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.info("detach -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }
}
