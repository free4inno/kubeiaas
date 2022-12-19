package kubeiaas.iaascore.request.image;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class SaveImageForm {
    @NotNull @NotEmpty
    String uuid;
    @NotNull
    String content;
}
