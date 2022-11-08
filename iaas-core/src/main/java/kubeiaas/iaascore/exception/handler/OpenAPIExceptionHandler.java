package kubeiaas.iaascore.exception.handler;

import kubeiaas.common.bean.Vm;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.VmException;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;

/**
 * Author HUYUZHU.
 * Date 2021/10/24 21:24.
 * 统一异常处理
 */

@Slf4j
@ControllerAdvice("kubeiaas.iaascore.openapi")
public class OpenAPIExceptionHandler {

    @Resource
    private TableStorage tableStorage;

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public BaseResponse handle(Exception e) {
        if (e instanceof BaseException) {
            // ----- base ------
            log.error(((BaseException) e).getMsg());
            return BaseResponse.error(ResponseEnum.WORK_ERROR);

        } else if (e instanceof VmException){
            // ----- vm error -----
            Vm vm = ((VmException) e).getVm();
            vm.setStatus(VmStatusEnum.ERROR);
            tableStorage.vmSave(vm);
            log.error(((VmException) e).getMsg());
            return BaseResponse.error(ResponseEnum.WORK_ERROR);

        } else if (e instanceof ConstraintViolationException ||
                e instanceof MethodArgumentNotValidException ||
                e instanceof MissingServletRequestParameterException) {
            // ----- args error -----
            log.error(ResponseEnum.ARGS_ERROR.getMsg());
            e.printStackTrace();
            return BaseResponse.error(ResponseEnum.ARGS_ERROR);

        } else {
            // ----- else -----
            log.error(ResponseEnum.INTERNAL_ERROR.getMsg());
            e.printStackTrace();
            return BaseResponse.error(ResponseEnum.INTERNAL_ERROR);
        }
    }

}
