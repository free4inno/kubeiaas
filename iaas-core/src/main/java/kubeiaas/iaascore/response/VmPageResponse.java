package kubeiaas.iaascore.response;

import kubeiaas.common.bean.Vm;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class VmPageResponse {
    List<Vm> content;
    int totalPages;
    long totalElements;
}
