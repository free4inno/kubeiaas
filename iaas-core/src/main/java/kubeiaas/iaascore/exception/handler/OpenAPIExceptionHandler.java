package kubeiaas.iaascore.exception.handler;

import kubeiaas.common.bean.Vm;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.VmException;
import kubeiaas.iaascore.exception.VolumeException;
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
            BaseException baseE = (BaseException) e;
            log.error(baseE.getMsg());
            return BaseResponse.error(baseE.getResEnum(), baseE.getMsg());

        } else if (e instanceof VmException){
            // ----- vm error -----
            VmException vmE = (VmException) e;
            Vm vm = (vmE.getVm());
            vm.setStatus(VmStatusEnum.ERROR);
            tableStorage.vmSave(vm);
            log.error(vmE.getMsg());
            return BaseResponse.error(vmE.getResEnum(), vmE.getMsg());

        } else if (e instanceof VolumeException){
            // ----- volume error -----
            VolumeException volE = (VolumeException) e;
            Volume volume = volE.getVolume();
            volume.setStatus(VolumeStatusEnum.ERROR);
            tableStorage.volumeSave(volume);
            log.error(volE.getMsg());
            return BaseResponse.error(volE.getResEnum(), volE.getMsg());

        } else if (e instanceof ConstraintViolationException ||
                e instanceof MethodArgumentNotValidException ||
                e instanceof MissingServletRequestParameterException) {
            // ----- args error -----
            log.error(ResponseEnum.ARGS_ERROR.getMsg());
            e.printStackTrace();
            return BaseResponse.error(ResponseEnum.ARGS_ERROR, ResponseEnum.ARGS_ERROR.getMsg());

        } else {
            // ----- else -----
            log.error(ResponseEnum.INTERNAL_ERROR.getMsg());
            e.printStackTrace();
            return BaseResponse.error(ResponseEnum.INTERNAL_ERROR);
        }
    }

}
