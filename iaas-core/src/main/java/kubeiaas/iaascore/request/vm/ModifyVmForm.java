package kubeiaas.iaascore.request.vm;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Slf4j
@Data
public class ModifyVmForm {

    @NotNull
    @NotEmpty
    private String vmUuid;

    private Integer cpus;

    private Integer memory;

}
