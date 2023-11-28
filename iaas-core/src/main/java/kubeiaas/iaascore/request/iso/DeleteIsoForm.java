package kubeiaas.iaascore.request.iso;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class DeleteIsoForm {
    @NotNull
    @NotEmpty
    private String uuid;
}
