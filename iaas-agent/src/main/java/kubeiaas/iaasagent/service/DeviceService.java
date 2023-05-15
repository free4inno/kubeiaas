package kubeiaas.iaasagent.service;

import kubeiaas.common.bean.Device;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.enums.device.DeviceStatusEnum;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.iaasagent.config.HostConfig;
import kubeiaas.iaasagent.config.LibvirtConfig;
import kubeiaas.iaasagent.config.XmlConfig;
import kubeiaas.iaasagent.utils.PCIUtils;
import kubeiaas.iaasagent.utils.USBUtils;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class DeviceService {

    @Resource
    private XmlConfig xmlConfig;

    public List<Device> queryAll() {
        List<Device> devList = new ArrayList<>();
        devList.addAll(USBUtils.getHostDevices());
        devList.addAll(PCIUtils.getHostDevices());
        devList.forEach(
            d -> {
                d.setStatus(DeviceStatusEnum.AVAILABLE);
                d.setHostUuid(HostConfig.thisHost.getUuid());
            }
        );
        return devList;
    }

    /**
     * 支持的设备操作类型
     */
    private static final class DeviceOperateFlags {
        public static final int ATTACH  = 0b0001;
        public static final int DETACH = 0b0010;
    }

    /**
     * 执行设备操作
     * @param device the device instance
     * @param vm the vm instance
     * @param flag the operation on your device
     * @return operate result
     */
    private boolean operate(Device device, Vm vm, int flag) {
        log.info("operateDevice ---- start ----");
        log.info(" -- device: " + device.toString() + "vm: " + vm.toString());
        String deviceXml = "";
        switch (device.getType()) {
            case USB:
                log.info(" -- USB Device");
                deviceXml = xmlConfig.getUsbDevice(device);
                break;
            case PCI:
                log.info(" -- PCI Device");
                deviceXml = xmlConfig.getPciDevice(device);
                break;
        }
        if (deviceXml.isEmpty()) {
            return false;
        }
        log.info(" -- operate device info: " + deviceXml);

        String vmUuid = vm.getUuid();
        try {
            Connect virtCon = LibvirtConfig.getVirtCon();
            Domain domain = virtCon.domainLookupByUUIDString(vmUuid);
            try {
                /**
                 * Attach / Detach a virtual device to a domain, using the flags parameter to control how the device is attached.
                 * - 0000: VIR_DOMAIN_AFFECT_CURRENT specifies that the device allocation is made based on current domain state.
                 * - 0001: VIR_DOMAIN_AFFECT_LIVE specifies that the device shall be allocated to the active domain instance only and is not added to the persisted domain configuration.
                 * - 0010: VIR_DOMAIN_AFFECT_CONFIG specifies that the device shall be allocated to the persisted domain configuration only. Note that the target hypervisor must return an error if unable to satisfy flags.
                 * - 0100: FORCE
                 * Use | to combine those configs, we got 3 as (VIR_DOMAIN_AFFECT_LIVE | VIR_DOMAIN_AFFECT_CONFIG)
                 */
                int virtFlag = vm.getStatus().equals(VmStatusEnum.ACTIVE) ? (0b0001 | 0b0010) : (0b0010);
                switch (flag) {
                    case DeviceOperateFlags.ATTACH:
                        domain.attachDeviceFlags(deviceXml, virtFlag);
                        break;

                    case DeviceOperateFlags.DETACH:
                        domain.detachDeviceFlags(deviceXml, virtFlag);
                        break;

                    default:
                        log.info("-- do nothing");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } catch (LibvirtException e) {
            e.printStackTrace();
            return false;
        }
        log.info("operateDevice ---- done ----");
        return true;
    }

    public boolean attach(Device device, Vm vm) {
        return this.operate(device, vm, DeviceOperateFlags.ATTACH);
    }

    public boolean detach(Device device, Vm vm) {
        return this.operate(device, vm, DeviceOperateFlags.DETACH);
    }
}
