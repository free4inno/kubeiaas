package kubeiaas.dbproxy.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.dbproxy.dao.VmDao;
import kubeiaas.dbproxy.table.VmTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.VM)
public class VmController {
    @Resource
    private VmDao vmDao;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("queryAll ==== start ====");
        List<VmTable> vmTableList = vmDao.findAll();
        log.info("queryAll ==== end ====");
        return JSON.toJSONString(vmTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SAVE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public void save(
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObjectStr) {
        log.info("save ==== start ====");
        VmTable vmTable = JSON.parseObject(vmObjectStr, VmTable.class);
        vmDao.save(vmTable);
        log.info("save ==== end ====");
    }
}
