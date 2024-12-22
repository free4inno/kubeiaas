package kubeiaas.iaascore.exception;

import kubeiaas.common.bean.Iso;
import kubeiaas.iaascore.response.ResponseEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IsoException extends Exception {
    public IsoException(String msg) {
        super(msg);
    }
    public IsoException(ResponseEnum resEnum) {
        super(resEnum.getMsg());
        this.resEnum = resEnum;
    }

    public IsoException(String msg, ResponseEnum resEnum) {
        super(msg);
        this.resEnum = resEnum;
    }

    private String msg;
    private ResponseEnum resEnum;
    private Iso iso;
}
