package kubeiaas.resourceoperator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ResourceOperatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResourceOperatorApplication.class, args);
    }

}
