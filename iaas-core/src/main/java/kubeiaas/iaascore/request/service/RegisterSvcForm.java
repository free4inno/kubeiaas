package kubeiaas.iaascore.request.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Slf4j
@Data
public class RegisterSvcForm {
    @NotNull @NotEmpty
    private String serviceName;

    @NotNull @NotEmpty
    private String nodeName;

    @NotNull
    private Long timestamp;
}
