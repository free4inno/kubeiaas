package kubeiaas.iaascore.request.vm;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Slf4j
@Data
public class PublishImageForm {
    @NotNull
    @NotEmpty
    private String vmUuid;

    @NotNull
    @NotEmpty
    private String name;

    private String description;

    @NotNull
    @Min(0)
    private Integer vdSize;
}
