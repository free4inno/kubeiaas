package kubeiaas.common.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import kubeiaas.common.constants.HufuServiceConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * 和知了交互的方法
 */
public class ZhiUtils {
    public static Optional<JSONArray> getAttachments(int id) {
        String zhiUrl = String.format("%s/openapi/detail?appkey=sYu1gpFF&id=%d", HufuServiceConstants.TEMPLATE_CENTER_URL, id);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(zhiUrl, String.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            return Optional.empty();
        }
        JSONObject zhiResponse = JSON.parseObject(responseEntity.getBody());
        int code = zhiResponse.getInteger("code");
        if (code != 200) {
            return Optional.empty();
        }
        JSONObject data = zhiResponse.getJSONObject("data");
        String attachmentJsonString = data.getString("attachment");
        JSONArray attachmentArray = JSON.parseArray(attachmentJsonString);

        return Optional.of(attachmentArray);
    }
}
