package kubeiaas.iaascore.openapi;

import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.iaascore.dao.TableStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Slf4j
@Validated
@Controller
@RequestMapping(value = "debug")
public class DebugOpenApi {
    @Resource
    private TableStorage tableStorage;

    @RequestMapping(value = "delete_iso_and_mount")
    @ResponseBody
    public String deleteIsoAndMount() {
        tableStorage.clearIsoTableAndMountTable();
        return "success";
    }
}
