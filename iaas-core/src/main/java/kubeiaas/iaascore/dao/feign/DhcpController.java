package kubeiaas.iaascore.dao.feign;

import kubeiaas.common.constants.ComponentConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.net.URI;

//@FeignClient(name = ComponentConstants.DHCP_CONTROLLER, url = "http://192.168.31.238:9090")
@FeignClient(name = ComponentConstants.DHCP_CONTROLLER, url = "EMPTY")
public interface DhcpController {

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DHCP_C + "/" + RequestMappingConstants.BIND_MAC_IP)
    @ResponseBody
    String bindMacAndIp(
            URI uri,
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid,
            @RequestParam(value = RequestParamConstants.MAC) String mac,
            @RequestParam(value = RequestParamConstants.IP) String ip);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DHCP_C + "/" + RequestMappingConstants.UNBIND_MAC_IP)
    @ResponseBody
    String unbindMacAndIp(
            URI uri,
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid);

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DHCP_C + "/" + RequestMappingConstants.UPDATE_IP_SEG)
    @ResponseBody
    String updateIpSeg(
            URI uri,
            @RequestParam(value = RequestParamConstants.IP_SEGMENT_ID) String ipSegId);

}
