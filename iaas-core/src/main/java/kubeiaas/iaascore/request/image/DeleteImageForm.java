package kubeiaas.iaascore.request.image;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class DeleteImageForm {
    @NotNull @NotEmpty
    String uuid;
}
