package kubeiaas.iaasagent.proxy;

import lombok.extern.slf4j.Slf4j;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class ProxyConfig {

    @Bean
    public ProxyServlet proxyServlet(){
        return new ProxyServlet();
    }

    @Bean
    public ServletRegistrationBean terry(@Autowired ProxyServlet proxyServlet) {
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(proxyServlet, "/*");
        //设置网址以及参数
        Map<String, String> params = new HashMap<>();
        params.put("targetUri", "http://db-proxy:9091");
        registrationBean.setInitParameters(params);
        return registrationBean;
    }
}
