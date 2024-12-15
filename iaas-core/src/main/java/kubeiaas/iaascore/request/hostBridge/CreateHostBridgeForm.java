package kubeiaas.iaascore.request.hostBridge;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Slf4j
@Data
public class CreateHostBridgeForm {
//    @NotNull @NotEmpty
//    private String hostUuid;

    @NotNull @NotEmpty
    private String bridgeName;

    @NotNull @NotEmpty
    private String ipSegmentId;

    @NotNull @NotEmpty
    private String hostName;
}
