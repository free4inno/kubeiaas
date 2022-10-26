package kubeiaas.imageoperator;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ImageOperatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImageOperatorApplication.class, args);
    }
}
