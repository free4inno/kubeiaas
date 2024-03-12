package kubeiaas.iaascore.request.vm;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class ColdMigrateVmForm {
    @NotNull
    @NotEmpty
    private String vmUuid;
    @NotNull
    @NotEmpty
    private String targetHostUuid;
    @NotNull
    @Min(1)
    private Integer targetPrivateIpSegmentId;

    private Integer targetPublicIpSegmentId;
}
