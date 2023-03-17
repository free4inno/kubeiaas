package kubeiaas.iaascore.scheduler;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Device;
import kubeiaas.common.bean.Host;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.dao.feign.DeviceController;
import kubeiaas.iaascore.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@Configuration
public class DeviceScheduler {
    @Resource
    private DeviceController deviceController;

    @Resource
    private TableStorage tableStorage;

    public List<Device> queryAll(Host host) throws BaseException {
        try {
            String jsonObjectString = deviceController.queryAll(getUri(host));
            return JSON.parseArray(jsonObjectString, Device.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException("err: device query all failed, host name is " + host.getName());
        }
    }

    private URI getUri(Host host) {
        try {
            return new URI(AgentConfig.getHostUri(host));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            log.error("build URI failed!");
            return null;
        }
    }

}
