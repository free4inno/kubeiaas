package kubeiaas.dhcpcontroller.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DhcpService {
    public boolean bindMacAndIp(String vmUuid, String mac, String ip) {
        // todo: Set DHCP-Controller in iaas-Agent!
        return true;
    }

}
