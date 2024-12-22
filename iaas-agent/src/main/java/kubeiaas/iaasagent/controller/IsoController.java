package kubeiaas.iaasagent.controller;

import com.alibaba.fastjson2.JSON;
import kubeiaas.common.bean.Iso;
import kubeiaas.common.bean.Mount;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.iaasagent.service.IsoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.net.URI;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.ISO_C)
public class IsoController {
    @Resource
    private IsoService isoService;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.ATTACH_ISO, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String attachIso(
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObjectStr,
            @RequestParam(value = RequestParamConstants.MOUNT_OBJECT) String mountObjectStr) {
        log.info("-- start -- attachIso");
        Vm vm = JSON.parseObject(vmObjectStr, Vm.class);
        Mount mount = JSON.parseObject(mountObjectStr, Mount.class);
        if (isoService.attachIso(vm, mount)) {
            log.info("-- end -- attachIso -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("-- end -- attachIso -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DETACH_ISO, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String detachIso(
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObjectStr,
            @RequestParam(value = RequestParamConstants.MOUNT_OBJECT) String mountObjectStr) {
        log.info("-- start -- detachIso");
        Vm vm = JSON.parseObject(vmObjectStr, Vm.class);
        Mount mount = JSON.parseObject(mountObjectStr, Mount.class);
        if (isoService.detachIso(vm, mount)) {
            log.info("-- end -- detachIso -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("-- end -- detachIso -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.UPLOAD_ISO)
    @ResponseBody
    public String uploadIso(
            @RequestParam(value = RequestParamConstants.NAME) String name,
            @RequestParam(value = RequestParamConstants.FILE_URL) String fileUrl) {
        log.info("uploadIso -- start");
        if (isoService.uploadIso(name, fileUrl)) {
            log.info("uploadIso -- end -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("uploadIso -- end -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_ISO)
    @ResponseBody
    public String deleteIso(@RequestParam(value = RequestParamConstants.UUID) String uuid) {
        log.info("deleteIso -- start");
        if (isoService.deleteIso(uuid)) {
            log.info("deleteIso -- end -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("deleteIso -- end -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }
}
