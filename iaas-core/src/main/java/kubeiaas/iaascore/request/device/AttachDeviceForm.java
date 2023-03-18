package kubeiaas.iaascore.request.device;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class AttachDeviceForm {
    @NotNull @NotEmpty
    String vmUuid;

    @NotNull @NotEmpty
    String type;

    @NotNull @NotEmpty
    String bus;
    @NotNull @NotEmpty
    String dev;
    @NotNull @NotEmpty
    String vendor;
    @NotNull @NotEmpty
    String product;
}
