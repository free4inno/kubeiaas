package kubeiaas.iaascore.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseEnum {
    SUCCESS(200, "业务处理成功"),
    ERROR(500, "业务处理失败");

    private Integer code;
    private String msg;
}
