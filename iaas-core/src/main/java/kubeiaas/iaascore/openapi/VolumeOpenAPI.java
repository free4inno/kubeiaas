package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.VolumeException;
import kubeiaas.iaascore.request.volume.AttachVolumeForm;
import kubeiaas.iaascore.request.volume.CreateVolumeForm;
import kubeiaas.iaascore.request.volume.DeleteVolumeForm;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.ResponseEnum;
import kubeiaas.iaascore.service.VolumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.VOLUME)
public class VolumeOpenAPI {

    @Resource
    private VolumeService volumeService;

    /**
     * 创建 “云硬盘”
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.CREATE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String create(@Valid @RequestBody CreateVolumeForm f) throws VolumeException {
        log.info("create ==== start ====");
        Volume newVolume = volumeService.createDataVolume(f.getName(), f.getDiskSize(), f.getDescription(), f.getHostUuid());
        log.info("create ==== end ====");
        return JSON.toJSONString(BaseResponse.success(newVolume));
    }

    /**
     * 删除 “云硬盘”
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DELETE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String delete(@Valid @RequestBody DeleteVolumeForm f) throws BaseException {
        log.info("delete ==== start ====");
        String result = volumeService.deleteDataVolume(f.getVolumeUuid());
        if (result.equals(ResponseMsgConstants.SUCCESS)) {
            Map<String, String> resData = new HashMap<>();
            resData.put("message", "Delete Data Volume Success");
            log.info("delete ==== end ====");
            return JSON.toJSONString(BaseResponse.success(resData));
        } else {
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VOLUME_DELETE_ERROR));
        }
    }

    /**
     * 挂载 “云硬盘”
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.ATTACH, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String attach(@Valid @RequestBody AttachVolumeForm f) throws BaseException {
        log.info("attach ==== start ====");
        String result = volumeService.attachDataVolume(f.getVmUuid(),f.getVolumeUuid());
        if (result.equals(ResponseMsgConstants.SUCCESS)) {
            Map<String, String> resData = new HashMap<>();
            resData.put("message", "attach Data Volume Success");
            log.info("attach ==== end ====");
            return JSON.toJSONString(BaseResponse.success(resData));
        } else {
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VOLUME_ATTACH_ERROR));
        }
    }

    /**
     * 卸载 “云硬盘”
     */
    @RequestMapping(method = RequestMethod.POST, value = RequestMappingConstants.DETACH, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String detach(@Valid @RequestBody AttachVolumeForm f) throws BaseException {
        log.info("detach ==== start ====");
        String result = volumeService.detachDataVolume(f.getVmUuid(),f.getVolumeUuid());
        if (result.equals(ResponseMsgConstants.SUCCESS)) {
            Map<String, String> resData = new HashMap<>();
            resData.put("message", "detach Data Volume Success");
            log.info("detach ==== end ====");
            return JSON.toJSONString(BaseResponse.success(resData));
        } else {
            return JSON.toJSONString(BaseResponse.error(ResponseEnum.VOLUME_DETACH_ERROR));
        }
    }

    /**
     * 获取 “云硬盘”列表
     */
    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("queryAll ==== start ====");
        List<Volume> dataVolumeList = volumeService.queryAllDataVolume();
        log.info("queryAll ==== end ====");
        return JSON.toJSONString(BaseResponse.success(dataVolumeList));
    }
}
