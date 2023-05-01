package kubeiaas.iaasagent.proxy.config;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ProxyConfig implements ServletContextInitializer {

    private ServletContext sc;

    @Override
    public void onStartup(ServletContext servletContext) {
        try {
            this.sc = servletContext;
            this.registerDBProxy();
            this.registerImageOperator();
            this.registerIaasCore();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void registerDBProxy() {
        ServletRegistration registrationBean = this.sc.addServlet("db-proxy", ProxyServletExtend.class);
        Map<String, String> initParameters = new LinkedHashMap<>();
        initParameters.put(ProxyServletExtend.P_LOG, "true");
        initParameters.put(ProxyServletExtend.P_TARGET_URI, "http://db-proxy:9091");
        registrationBean.setInitParameters(initParameters);
        registrationBean.addMapping("/db_proxy/*");
    }

    protected void registerImageOperator() {
        ServletRegistration registrationBean = this.sc.addServlet("image-operator", ProxyServletExtend.class);
        Map<String, String> initParameters = new LinkedHashMap<>();
        initParameters.put(ProxyServletExtend.P_LOG, "true");
        initParameters.put(ProxyServletExtend.P_TARGET_URI, "http://image-operator:9093");
        registrationBean.setInitParameters(initParameters);
        registrationBean.addMapping("/image_operator/*");
    }

    protected void registerIaasCore() {
        ServletRegistration registrationBean = this.sc.addServlet("iaas-core", ProxyServletExtend.class);
        Map<String, String> initParameters = new LinkedHashMap<>();
        initParameters.put(ProxyServletExtend.P_LOG, "true");
        initParameters.put(ProxyServletExtend.P_TARGET_URI, "http://iaas-core:9080");
        registrationBean.setInitParameters(initParameters);
        registrationBean.addMapping("/iaas_core/*");
    }
}
