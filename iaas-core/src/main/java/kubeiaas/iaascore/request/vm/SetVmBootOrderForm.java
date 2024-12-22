package kubeiaas.iaascore.request.vm;

import kubeiaas.common.enums.vm.VmBootEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class SetVmBootOrderForm {
    @NotNull
    @NotEmpty
    private String vmUuid;
    @NotNull
    private VmBootEnum bootOrder;
}
