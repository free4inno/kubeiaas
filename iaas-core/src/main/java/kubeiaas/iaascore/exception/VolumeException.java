package kubeiaas.iaascore.exception;

import kubeiaas.common.bean.Volume;
import kubeiaas.iaascore.response.ResponseEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 云硬盘异常处理类
 */
@Data
@AllArgsConstructor
public class VolumeException extends Exception {
    private Volume volume;
    private String msg;
    private ResponseEnum resEnum;

    public VolumeException(Volume volume, String msg) {
        this.volume = volume;
        this.msg = msg;
        this.resEnum = ResponseEnum.WORK_ERROR;
    }
}
