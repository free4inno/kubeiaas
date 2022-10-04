package kubeiaas.iaasagent.config;

import kubeiaas.common.constants.bean.HostConstants;
import kubeiaas.common.utils.ShellUtils;
import kubeiaas.iaasagent.service.HostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Slf4j
@Configuration
public class VncConfig {
    public static final String TOKEN_FILE_PATH = "/usr/local/kubeiaas/vnc/token/token.conf";
    public static final String START_VNC_CLIENT_CMD = "python3 /usr/local/kubeiaas/vnc/websockify-0.10.0/websockify -D --web=/usr/local/kubeiaas/vnc/noVNC-1.3.0/ 8787 --target-config=/usr/local/kubeiaas/vnc/token/token.conf";
    public static final String NEW_SCREEN_CMD = "screen -R vnc";
    public static final String CHECK_VNC_PORT = "netstat -nlt | grep 8787 | wc -l";
}
