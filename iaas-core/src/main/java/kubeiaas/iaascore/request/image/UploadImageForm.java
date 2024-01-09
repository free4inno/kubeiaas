package kubeiaas.iaascore.request.image;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class UploadImageForm {
    @NotNull @Min(1)
    int id;
}
