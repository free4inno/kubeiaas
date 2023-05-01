package kubeiaas.iaascore.scheduler;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Image;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.iaascore.dao.feign.ImageOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Slf4j
@Configuration
public class ImageScheduler {

    @Resource
    private ImageOperator imageOperator;

    public boolean imageCreateYaml(Image image) {
        String imageObjectStr = JSON.toJSONString(image);
        try {
            return imageOperator.imageCreateYaml(imageObjectStr).equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
