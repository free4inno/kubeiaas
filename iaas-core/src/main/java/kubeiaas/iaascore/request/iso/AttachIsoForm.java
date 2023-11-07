package kubeiaas.iaascore.request.iso;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class AttachIsoForm {
    @NotNull
    @NotEmpty
    private String vmUuid;

    @NotNull
    @NotEmpty
    private String isoUuid;

    // mount method, such as virtio, ide, scsi, etc.
    private String method;
}
