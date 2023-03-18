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
                /**
                 * Attach a virtual device to a domain, using the flags parameter to control how the device is attached.
                 * - 0000: VIR_DOMAIN_AFFECT_CURRENT specifies that the device allocation is made based on current domain state.
                 * - 0001: VIR_DOMAIN_AFFECT_LIVE specifies that the device shall be allocated to the active domain instance only and is not added to the persisted domain configuration.
                 * - 0010: VIR_DOMAIN_AFFECT_CONFIG specifies that the device shall be allocated to the persisted domain configuration only. Note that the target hypervisor must return an error if unable to satisfy flags.
                 * - 0100: FORCE
                 * Use | to combine those configs, we got 3 as (VIR_DOMAIN_AFFECT_LIVE | VIR_DOMAIN_AFFECT_CONFIG)
                 */
                domain.attachDeviceFlags(deviceXml, (0b0001 | 0b0010));
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
