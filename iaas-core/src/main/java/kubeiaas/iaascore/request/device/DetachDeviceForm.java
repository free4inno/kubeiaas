package kubeiaas.iaascore.request.device;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class DetachDeviceForm {
    @NotNull @NotEmpty
    String vmUuid;

    @NotNull @NotEmpty
    String type;

    /**
     * A sign to identity union device.
     *
     * USB DEVICE:
     *      bus:dev:vendor:product
     * PCI DEVICE:
     *      domain:bus:slot:function
     */
    @NotNull @NotEmpty
    String sign;
}
