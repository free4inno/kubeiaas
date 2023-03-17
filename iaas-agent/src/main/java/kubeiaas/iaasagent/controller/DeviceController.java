package kubeiaas.iaasagent.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Device;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.iaasagent.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
}
