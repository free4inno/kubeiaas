package kubeiaas.iaascore.request.volume;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Slf4j
@Data
public class ResizeVolumeForm {
    @NotNull
    @NotEmpty
    private String volumeUuid;
    @NotNull
    @DecimalMin("0.00")
    private int diskSize;
}
