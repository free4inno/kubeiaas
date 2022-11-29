package kubeiaas.common.bean;

import kubeiaas.common.enums.config.SpecTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecConfig {
    private Integer id;
    private SpecTypeEnum type;
    private String value;
    private String description;
}
