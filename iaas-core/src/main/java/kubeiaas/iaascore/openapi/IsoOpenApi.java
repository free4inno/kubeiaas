package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson2.JSON;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.IsoException;
import kubeiaas.iaascore.request.iso.AttachIsoForm;
import kubeiaas.iaascore.request.iso.DeleteIsoForm;
import kubeiaas.iaascore.request.iso.DetachIsoForm;
import kubeiaas.iaascore.request.iso.UploadIsoForm;
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

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.UPLOAD, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String upload(@Valid @RequestBody UploadIsoForm f) throws IsoException, BaseException {
        log.info("-- start -- uploadIso");
        String result;
        switch (f.type) {
            case "local":
                result= isoService.uploadFromLocal(f.isoName);
                break;
            case "zhi":
                result= isoService.uploadFromZhi(f.templateId);
                break;
            default:
                throw new IsoException("type is not supported");
        }
        log.info("-- end -- uploadIso -- success");
        return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(result)));
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DELETE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String delete(@Valid @RequestBody DeleteIsoForm f) throws IsoException, BaseException {
        log.info("-- start -- deleteIso");
        String result = isoService.deleteIso(f.getUuid());
        log.info("-- end -- deleteIso -- success");
        return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(result)));
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.ATTACH, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String attach(@Valid @RequestBody AttachIsoForm f) throws IsoException, BaseException {
        log.info("-- start -- attachIso");
        if (f.getMethod() == null) {
            f.setMethod("virtio");
        }
        String result = isoService.attachIso(f.getVmUuid(), f.getIsoUuid(), f.getMethod());
        log.info("-- end -- attachIso -- success");
        return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(result)));
    }

    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DETACH, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String detach(@Valid @RequestBody DetachIsoForm f) throws IsoException, BaseException {
        log.info("-- start -- detachIso");
        String result = isoService.detachIso(f.getVmUuid(), f.getIsoUuid());
        log.info("-- end -- detachIso -- success");
        return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(result)));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_BY_UUID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryByUuid(@RequestParam(value = "uuid") String isoUuid) throws IsoException, BaseException {
        log.info("-- start -- queryByUuid");
        String result = isoService.queryByUuid(isoUuid);
        log.info("-- end -- queryByUuid -- success");
        return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(result)));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() throws IsoException, BaseException {
        log.info("-- start -- queryAll");
        String result = isoService.queryAll();
        log.info("-- end -- queryAll -- success");
        return JSON.toJSONString(BaseResponse.success(new SingleMsgResponse(result)));
    }
}
