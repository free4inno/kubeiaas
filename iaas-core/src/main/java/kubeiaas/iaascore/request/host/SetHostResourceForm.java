package kubeiaas.iaascore.request.host;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class SetHostResourceForm {
    @NotNull @NotEmpty
    String hostUuid;

    @NotNull @Min(0)
    Integer vcpu;
    @NotNull @Min(0)
    Integer mem;
    @NotNull @Min(0)
    Integer storage;
}
