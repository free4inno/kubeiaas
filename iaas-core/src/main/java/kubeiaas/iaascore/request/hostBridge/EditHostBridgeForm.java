package kubeiaas.iaascore.request.hostBridge;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Slf4j
@Data
public class EditHostBridgeForm {
    @NotNull @NotEmpty
    private Integer hostBridgeId;

    @NotNull @NotEmpty
    private String bridgeName;

    @NotNull @NotEmpty
    private Integer ipSegmentId;

    @NotNull @NotEmpty
    private String hostName;
}
