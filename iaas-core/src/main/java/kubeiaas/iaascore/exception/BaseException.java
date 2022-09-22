package kubeiaas.iaascore.exception;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 异常处理类
 */
@Data
@AllArgsConstructor
public class BaseException extends Exception{
    private String msg;
}
