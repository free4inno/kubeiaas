package kubeiaas.iaascore.config;

import kubeiaas.iaascore.interceptor.LogInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class IaasCoreConfigurer implements WebMvcConfigurer {

    @Bean
    public HandlerInterceptor getLogInterceptor() {
        return new LogInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(getLogInterceptor())
                .addPathPatterns("/**");
    }
}
