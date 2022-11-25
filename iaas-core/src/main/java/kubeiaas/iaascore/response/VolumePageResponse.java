package kubeiaas.iaascore.response;

import kubeiaas.common.bean.Volume;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class VolumePageResponse {
    List<Volume> content;
    int totalPages;
    long totalElements;
}
