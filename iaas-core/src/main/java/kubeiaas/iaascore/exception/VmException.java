package kubeiaas.iaascore.exception;


import kubeiaas.common.bean.Vm;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Vm异常处理类
 */
@Data
@AllArgsConstructor
public class VmException extends Exception{
    private Vm vm;
    private String msg;
}
