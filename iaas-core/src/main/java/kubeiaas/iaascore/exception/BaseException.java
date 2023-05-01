package kubeiaas.iaascore.exception;

import kubeiaas.iaascore.response.ResponseEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 异常处理类
 */
@Data
@AllArgsConstructor
public class BaseException extends Exception{
    private String msg;
    private ResponseEnum resEnum;

    public BaseException(String msg) {
        this.msg = msg;
        this.resEnum = ResponseEnum.WORK_ERROR;
    }
}
