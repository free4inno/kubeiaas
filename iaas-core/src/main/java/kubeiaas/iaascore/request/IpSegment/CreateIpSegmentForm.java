package kubeiaas.iaascore.request.IpSegment;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Slf4j
@Data
public class CreateIpSegmentForm {
    @NotNull @NotEmpty
    private String name;

    @NotNull @NotEmpty
    private String hostUuid;

    @NotNull @NotEmpty
    private String type;

    @NotNull @NotEmpty
    private String bridge;

    @NotNull @NotEmpty
    private String ipRangeStart;

    @NotNull @NotEmpty
    private String ipRangeEnd;

    @NotNull @NotEmpty
    private String gateway;

    @NotNull @NotEmpty
    private String netmask;

}
