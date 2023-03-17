package kubeiaas.common.bean;

import kubeiaas.common.enums.device.DeviceStatusEnum;
import kubeiaas.common.enums.device.DeviceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    private Integer id;
    private DeviceTypeEnum type;

    private String bus;
    private String device;
    private String vendor;
    private String product;

    private String hostUuid;
    private String name;

    private DeviceStatusEnum status;
    private String instanceUuid;

    public Device(DeviceTypeEnum type) {
        this.setType(type);
    }

    public boolean equals(Device dev) {
        if (null == dev)
            return false;
        if (null == bus || null == dev.getBus() || !bus.equals(dev.getBus()))
            return false;
        if (null == device || null == dev.getDevice() || !device.equals(dev.getDevice()))
            return false;
        if (null == vendor || null == dev.getVendor() || !vendor.equals(getVendor()))
            return false;
        if (null == product || null == dev.getProduct() || !product.equals(getProduct()))
            return false;
        return true;
    }
}
