package kubeiaas.iaascore.exception;


import kubeiaas.common.bean.Vm;
import kubeiaas.iaascore.response.ResponseEnum;
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
    private ResponseEnum resEnum;

    public VmException(Vm vm, String msg) {
        this.vm = vm;
        this.msg = msg;
        this.resEnum = ResponseEnum.WORK_ERROR;
    }
}
