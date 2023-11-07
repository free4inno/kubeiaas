package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.IsoException;
import kubeiaas.iaascore.request.iso.AttachIsoForm;
import kubeiaas.iaascore.request.iso.DetachIsoForm;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.SingleMsgResponse;
import kubeiaas.iaascore.service.IsoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Slf4j
@Validated
@Controller
@RequestMapping(value = RequestMappingConstants.ISO)
public class IsoOpenApi {
    @Resource
    private IsoService isoService;

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.ATTACH, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String attach(@Valid @RequestBody AttachIsoForm f) throws IsoException, BaseException {
        log.info("attach ==== start ====");
        if (f.getMethod() == null) {
            f.setMethod("virtio");
        }
        if (f.getMethod().equals("virtio")) {
            String result = isoService.attachIsoAsVirtio(f.getVmUuid(), f.getIsoUuid());
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(result)));
        } else if (f.getMethod().equals("scsi")) {
            String result = isoService.attachIsoAsScsi(f.getVmUuid(), f.getIsoUuid());
            return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(result)));
        } else {
            throw new IsoException("Unsupported mount method: " + f.getMethod());
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DETACH, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String detach(@Valid @RequestBody DetachIsoForm f) throws IsoException, BaseException {
        log.info("detach ==== start ====");
        String result = isoService.detachIso(f.getVmUuid(), f.getIsoUuid());
        log.info("detach ==== end ====");
        return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(result)));
    }

    @RequestMapping(method = RequestMethod.POST, value = "test_upload", produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String testUpload() throws IsoException, BaseException {
        log.info("testUpload ==== start ====");
        String result = isoService.uploadIsoToDatabase("", "test.iso");
        log.info("Mount Point: " + result);
        return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(ResponseMsgConstants.SUCCESS)));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_BY_UUID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryByUuid(@RequestParam(value = "uuid") String isoUuid) throws IsoException, BaseException {
        log.info("queryByUuid ==== start ====");
        log.info("isoUuid: {}", isoUuid);
        String result = isoService.queryByUuid(isoUuid);
        log.info("queryByUuid ==== end ====");
        return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(result)));
    }
}
