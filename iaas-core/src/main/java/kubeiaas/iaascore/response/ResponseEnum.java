package kubeiaas.iaascore.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OpenAPI 返回值状态对应的响应码和信息
 */
@Getter
@AllArgsConstructor
public enum ResponseEnum {
    SUCCESS(200, "业务处理成功"),
    WORK_ERROR(202, "业务处理失败"),
    ARGS_ERROR(402, "验参失败"),
    INTERNAL_ERROR(500, "内部错误");

    private Integer code;
    private String msg;
}
