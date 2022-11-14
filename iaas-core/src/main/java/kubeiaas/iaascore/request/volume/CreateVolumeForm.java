package kubeiaas.iaascore.request.volume;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
@Slf4j
@Data
public class CreateVolumeForm {
    @NotNull
    @NotEmpty
    private String name;

    @Min(1)
    private Integer diskSize;
    private String description;
    private String hostUuid;
}
