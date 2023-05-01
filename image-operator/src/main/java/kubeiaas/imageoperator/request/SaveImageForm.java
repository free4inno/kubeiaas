package kubeiaas.imageoperator.request;

import lombok.Data;

@Data
public class SaveImageForm {
    String uuid;
    String content;
}
