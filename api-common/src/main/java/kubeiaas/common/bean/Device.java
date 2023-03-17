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
    private String dev;
    private String vendor;
    private String product;

    private String hostUuid;
    private String name;

    private DeviceStatusEnum status;
    private String instanceUuid;

    public Device(DeviceTypeEnum type) {
        this.setType(type);
    }

    public boolean equals(Device device) {
        if (null == device)
            return false;
        if (null == type || null == device.getType() || !type.equals(device.getType()))
            return false;
        if (null == bus || null == device.getBus() || !bus.equals(device.getBus()))
            return false;
        if (null == dev || null == device.getDev() || !dev.equals(device.getDev()))
            return false;
        if (null == vendor || null == device.getVendor() || !vendor.equals(device.getVendor()))
            return false;
        if (null == product || null == device.getProduct() || !product.equals(device.getProduct()))
            return false;
        return true;
    }
}
