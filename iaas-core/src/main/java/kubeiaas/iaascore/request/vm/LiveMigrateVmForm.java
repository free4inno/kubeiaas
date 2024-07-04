package kubeiaas.iaascore.request.vm;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class LiveMigrateVmForm {
    @NotNull
    @NotEmpty
    private String vmUuid;
    @NotNull
    @NotEmpty
    private String targetHostUuid;
}
