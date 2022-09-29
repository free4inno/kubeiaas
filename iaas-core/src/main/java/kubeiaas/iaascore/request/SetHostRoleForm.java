package kubeiaas.iaascore.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class SetHostRoleForm {
    @NotNull @NotEmpty
    private String hostUuid;
    @NotNull @NotEmpty
    private String role;
}
