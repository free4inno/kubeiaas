package kubeiaas.imageoperator.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ImageConfig {
    public static final String STORAGE_BASE_DIR = "/usr/local/kubeiaas/data/";
    public static final String STORAGE_IMAGE_PATH = "images/";
}
