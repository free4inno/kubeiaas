package kubeiaas.iaasagent.service;

import kubeiaas.common.bean.Device;
import kubeiaas.common.enums.device.DeviceStatusEnum;
import kubeiaas.iaasagent.config.HostConfig;
import kubeiaas.iaasagent.utils.UsbUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DeviceService {

    public List<Device> queryAll() {
        List<Device> devList = UsbUtils.getUsbDevice();
        for (Device device : devList) {
            device.setStatus(DeviceStatusEnum.AVAILABLE);
            device.setHostUuid(HostConfig.thisHost.getUuid());
        }
        return devList;
    }
}
