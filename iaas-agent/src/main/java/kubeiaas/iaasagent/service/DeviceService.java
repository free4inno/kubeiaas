package kubeiaas.iaasagent.service;

import kubeiaas.common.bean.Device;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.enums.device.DeviceStatusEnum;
import kubeiaas.iaasagent.config.HostConfig;
import kubeiaas.iaasagent.config.XmlConfig;
import kubeiaas.iaasagent.utils.UsbUtils;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static kubeiaas.iaasagent.config.LibvirtConfig.virtCon;

@Slf4j
@Service
public class DeviceService {

    @Resource
    private XmlConfig xmlConfig;

    public List<Device> queryAll() {
        List<Device> devList = UsbUtils.getUsbDevice();
        for (Device device : devList) {
            device.setStatus(DeviceStatusEnum.AVAILABLE);
            device.setHostUuid(HostConfig.thisHost.getUuid());
        }
        return devList;
    }

    public boolean attach(Device device, Vm vm) {
        log.info("attachDevice ---- start ----");
        log.info(" -- device: " + device.toString() + "vm: " + vm.toString());
        String deviceXml = "";
        switch (device.getType()) {
            case USB:
                log.info(" -- USB Device");
                deviceXml = xmlConfig.getUsbDevice(device);
                break;
            case PCI:
                log.info(" -- PCI Device");
                break;
        }
        if (deviceXml.isEmpty()) {
            return false;
        }
        log.info(" -- attach device info: " + deviceXml);

        String vmUuid = vm.getUuid();
        try {
            Domain domain = virtCon.domainLookupByUUIDString(vmUuid);
            try {
                domain.attachDeviceFlags(deviceXml, 3);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } catch (LibvirtException e) {
            e.printStackTrace();
            return false;
        }
        log.info("attachDevice ---- end ----");
        return true;
    }
}
