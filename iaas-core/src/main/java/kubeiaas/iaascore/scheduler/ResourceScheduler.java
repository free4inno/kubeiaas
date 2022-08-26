package kubeiaas.iaascore.scheduler;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Host;
import kubeiaas.iaascore.dao.feign.ResourceOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Slf4j
@Configuration
public class ResourceScheduler {
    @Resource
    private ResourceOperator resourceOperator;

    public Host vmSelectHostByAppoint(String vmUuid, String hostUuid) {
        String hostObjectStr = resourceOperator.selectHostByAppoint(vmUuid, hostUuid);
        return JSON.parseObject(hostObjectStr, Host.class);
    }

    public Host vmSelectHostByOperator(String vmUuid, String strategy) {
        String hostObjectStr = resourceOperator.selectHostByOperator(vmUuid, strategy);
        return JSON.parseObject(hostObjectStr, Host.class);
    }
}
