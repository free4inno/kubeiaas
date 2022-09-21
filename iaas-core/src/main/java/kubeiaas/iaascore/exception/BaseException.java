package kubeiaas.iaascore.exception;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BaseException extends Exception{
    private String msg;
}
