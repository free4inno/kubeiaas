package kubeiaas.dhcpcontroller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DhcpControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DhcpControllerApplication.class, args);
    }

}
