package kubeiaas.iaascore.request.specConfig;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Slf4j
@Data
public class CreateSpecConfigForm {
    @NotNull @NotEmpty
    private String type;

    @NotNull @NotEmpty
    private String value;

    @NotNull @NotEmpty
    private String description;

}
