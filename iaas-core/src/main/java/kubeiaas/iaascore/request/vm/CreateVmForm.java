package kubeiaas.iaascore.request.vm;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Slf4j
@Data
public class CreateVmForm {
    @NotNull @NotEmpty
    private String name;
    @NotNull @Min(1)
    private int cpus;
    @NotNull @Min(1)
    private int memory;
    @NotNull @NotEmpty
    private String imageUuid;
    @NotNull @Min(1)
    private int ipSegmentId;

    private Integer publicIpSegId;

    @Min(1)
    private Integer diskSize;

    private String description;
    private String hostUuid;
}
