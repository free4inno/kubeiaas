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
        log.info("bindMacAndIp ==== " + "instanceUuid: " + vmUuid + " mac: " + mac + " ip: " + ip);
        if (dhcpService.bindMacAndIp(vmUuid, mac, ip)) {
            return ResponseMsgConstants.SUCCESS;
        } else {
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.UNBIND_MAC_IP, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String unbindMacAndIp(
            @RequestParam(value = RequestParamConstants.VM_UUID) @NotNull @NotEmpty String vmUuid) {
        log.info("bindMacAndIp ==== " + "instanceUuid: " + vmUuid);
        if (dhcpService.unbindMacAndIp(vmUuid)) {
            return ResponseMsgConstants.SUCCESS;
        } else {
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.UPDATE_IP_SEG, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String updateIpSeg(
            @RequestParam(value = RequestParamConstants.IP_SEGMENT_ID) @NotNull @NotEmpty String ipSegId) {
        log.info("updateIpSeg ==== " + "ipSegId: " + ipSegId);
        if (dhcpService.updateIpSeg(Integer.parseInt(ipSegId))) {
            return ResponseMsgConstants.SUCCESS;
        } else {
            return ResponseMsgConstants.FAILED;
        }
    }
}
