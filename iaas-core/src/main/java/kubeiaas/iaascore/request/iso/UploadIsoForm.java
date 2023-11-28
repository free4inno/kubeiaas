package kubeiaas.iaascore.request.iso;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class UploadIsoForm {
    @Min(0)
    public int templateId;

    public String isoName;

    public String type;
}
