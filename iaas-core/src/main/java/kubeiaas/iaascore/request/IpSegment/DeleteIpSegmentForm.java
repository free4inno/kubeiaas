package kubeiaas.iaascore.request.ipSegment;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;

@Slf4j
@Data
public class DeleteIpSegmentForm {
    @NotNull
    private Integer ipSegmentId;
}
