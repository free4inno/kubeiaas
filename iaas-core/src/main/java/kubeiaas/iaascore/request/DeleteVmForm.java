package kubeiaas.iaascore.request;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Slf4j
@Data
public class DeleteVmForm {

    @NotNull
    @NotEmpty
    private String vmUuid;
    @NotNull
    private String deleteType;

}
