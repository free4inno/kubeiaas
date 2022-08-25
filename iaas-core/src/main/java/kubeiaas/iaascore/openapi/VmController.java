package kubeiaas.iaascore.openapi;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.service.VmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.VM)
public class VmController {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private VmService vmService;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.TEST, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String test(HttpServletRequest request) {
        log.info("test ==== start ====");
        log.info("URI " + request.getRemoteAddr() + " " + request.getRemoteHost() + " " + request.getRemotePort());
        log.info("test ==== end ====");
        return "hello";
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.CREATE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String create(
            @RequestParam(value = RequestParamConstants.NAME) String name,
            @RequestParam(value = RequestParamConstants.CPUS) int cpus,
            @RequestParam(value = RequestParamConstants.MEMORY) int memory,
            @RequestParam(value = RequestParamConstants.IMAGE_UUID) String imageUuid,
            @RequestParam(value = RequestParamConstants.DISK_SIZE, required = false) Integer diskSize,
            @RequestParam(value = RequestParamConstants.DESCRIPTION, required = false) String description,
            @RequestParam(value = RequestParamConstants.HOST_UUID, required = false) String hostUuid) {
        log.info("create ==== start ====");
        vmService.createVm(name, cpus, memory, imageUuid, diskSize, description, hostUuid);
        log.info("create ==== end ====");
        return "success";
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("queryAll ==== start ====");
        List<Vm> vmList = tableStorage.vmQueryAll();
        log.info("queryAll ==== end ====");
        return JSON.toJSONString(vmList);
    }

}
