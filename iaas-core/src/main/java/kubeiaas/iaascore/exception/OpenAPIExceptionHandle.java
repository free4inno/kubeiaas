package kubeiaas.iaascore.exception;

import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolationException;

/**
 * Author HUYUZHU.
 * Date 2021/10/24 21:24.
 * 统一异常处理
 */

@Slf4j
@ControllerAdvice("kubeiaas.iaascore.openapi")
public class OpenAPIExceptionHandle {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public BaseResponse handle(Exception e) {
        if (e instanceof BaseException) {
            log.error(((BaseException) e).getMsg());
            return BaseResponse.error(ResponseEnum.WORK_ERROR);
        } else if (e instanceof ConstraintViolationException ||
                e instanceof MethodArgumentNotValidException ||
                e instanceof MissingServletRequestParameterException) {
            log.error(ResponseEnum.ARGS_ERROR.getMsg());
            e.printStackTrace();
            return BaseResponse.error(ResponseEnum.ARGS_ERROR);
        } else {
            log.error(ResponseEnum.INTERNAL_ERROR.getMsg());
            e.printStackTrace();
            return BaseResponse.error(ResponseEnum.INTERNAL_ERROR);
        }
    }

}
