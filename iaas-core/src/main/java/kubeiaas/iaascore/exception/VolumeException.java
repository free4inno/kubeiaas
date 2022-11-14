package kubeiaas.iaascore.exception;

import kubeiaas.common.bean.Volume;
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
}
