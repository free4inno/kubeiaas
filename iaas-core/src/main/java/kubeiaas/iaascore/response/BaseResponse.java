package kubeiaas.iaascore.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * OpenAPI 统一返回类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse {
    private Object data;
    private String message;
    private Float code;

    public static BaseResponse success(Object data){
        BaseResponse response = new BaseResponse();
        response.setData(data);
        response.setCode(ResponseEnum.SUCCESS.getCode());
        response.setMessage(ResponseEnum.SUCCESS.getMsg());
        return response;
    }

    public static BaseResponse error(ResponseEnum responseEnum){
        BaseResponse response = new BaseResponse();
        response.setData(null);
        response.setCode(responseEnum.getCode());
        response.setMessage(responseEnum.getMsg());
        return response;
    }
}
