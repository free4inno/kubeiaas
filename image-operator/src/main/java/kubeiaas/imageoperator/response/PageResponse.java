package kubeiaas.imageoperator.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResponse<T> {
    List<T> content;
    int totalPages;
    long totalElements;
}
