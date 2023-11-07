package kubeiaas.iaascore.request.iso;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class DetachIsoForm {
    @NotNull
    @NotEmpty
    private String vmUuid;

    @NotNull
    @NotEmpty
    private String isoUuid;
}
