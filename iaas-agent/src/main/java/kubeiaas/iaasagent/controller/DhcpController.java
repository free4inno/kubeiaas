package kubeiaas.iaasagent.controller;

import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.iaasagent.service.DhcpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.DHCP_C)
public class DhcpController {

    @Resource
    private DhcpService dhcpService;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.BIND_MAC_IP, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String bindMacAndIp(
            @RequestParam(value = RequestParamConstants.VM_UUID) @NotNull @NotEmpty String vmUuid,
            @RequestParam(value = RequestParamConstants.MAC) @NotNull @NotEmpty String mac,
            @RequestParam(value = RequestParamConstants.IP) @NotNull @NotEmpty String ip) {
        log.info("-- start -- bindMacAndIp");
        if (dhcpService.bindMacAndIp(vmUuid, mac, ip)) {
            log.info("-- end -- bindMacAndIp -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.info("-- end -- bindMacAndIp -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.UNBIND_MAC_IP, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String unbindMacAndIp(
            @RequestParam(value = RequestParamConstants.VM_UUID) @NotNull @NotEmpty String vmUuid) {
        log.info("-- start -- unbindMacAndIp");
        if (dhcpService.unbindMacAndIp(vmUuid)) {
            log.info("-- end -- unbindMacAndIp -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.info("-- end -- unbindMacAndIp -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.UPDATE_IP_SEG, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String updateIpSeg(
            @RequestParam(value = RequestParamConstants.IP_SEGMENT_ID) @NotNull @NotEmpty String ipSegId) {
        log.info("-- start -- updateIpSeg");
        if (dhcpService.updateIpSeg(Integer.parseInt(ipSegId))) {
            log.info("-- end -- updateIpSeg -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.info("-- end -- updateIpSeg -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }
}
