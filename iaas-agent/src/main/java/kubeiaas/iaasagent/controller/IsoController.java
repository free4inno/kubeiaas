package kubeiaas.iaasagent.controller;

import com.alibaba.fastjson.JSON;
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
        log.info("attachIso ==== " + "VM_OBJECT: " + vmObjectStr + " MOUNT_OBJECT: " + mountObjectStr);
        Vm vm = JSON.parseObject(vmObjectStr, Vm.class);
        Mount mount = JSON.parseObject(mountObjectStr, Mount.class);
        if (isoService.attachIso(vm, mount)) {
            log.info("attachIso -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("attachIso -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DETACH_ISO, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String detachIso(
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObjectStr,
            @RequestParam(value = RequestParamConstants.MOUNT_OBJECT) String mountObjectStr) {
        log.info("detachIso ==== " + "VM_OBJECT: " + vmObjectStr + " MOUNT_OBJECT: " + mountObjectStr);
        Vm vm = JSON.parseObject(vmObjectStr, Vm.class);
        Mount mount = JSON.parseObject(mountObjectStr, Mount.class);
        if (isoService.detachIso(vm, mount)) {
            log.info("detachIso -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("detachIso -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.UPLOAD_ISO)
    @ResponseBody
    String uploadIso(
            @RequestParam(value = RequestParamConstants.ISO_OBJECT) String isoObjectStr) {
        log.info("uploadIso ==== " + "ISO_OBJECT: " + isoObjectStr);
        Iso iso = JSON.parseObject(isoObjectStr, Iso.class);
        if (!isoService.isIsoExistsInServer(iso.getAbsolutePath())) {
            log.error("uploadIso: iso file not found, isoPath: {}, isoName: {}", iso.getRelativePath(), iso.getName());
            return ResponseMsgConstants.FAILED;
        }
        iso.setSize(isoService.getIsoSizeInBytes(iso.getAbsolutePath()).orElse(0L));
        if (isoService.uploadIso(iso)) {
            log.info("uploadIso -- success");
            return ResponseMsgConstants.SUCCESS;
        } else {
            log.error("uploadIso -- failed");
            return ResponseMsgConstants.FAILED;
        }
    }
}
