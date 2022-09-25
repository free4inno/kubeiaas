package kubeiaas.iaasagent.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class VncConfig {
    public static final String TOKEN_FILE_PATH = "/usr/local/kubeiaas/vnc/token/token.conf";
    public static final String START_VNC_CLIENT_CMD = "python3 /usr/local/kubeiaas/vnc/websockify-0.10.0/websockify -D --web=/usr/local/kubeiaas/vnc/noVNC-1.3.0/ 8787 --target-config=/usr/local/kubeiaas/vnc/token/token.conf";
    public static final String NEW_SCREEN_CMD = "screen -R vnc";
}
