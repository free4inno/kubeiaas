package kubeiaas.iaasagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class IaasAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(IaasAgentApplication.class, args);
    }

}
