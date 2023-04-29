package kubeiaas.iaascore.request.specConfig;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;

@Slf4j
@Data
public class DeleteSpecConfigForm {
    @NotNull
    private Integer id;
}
