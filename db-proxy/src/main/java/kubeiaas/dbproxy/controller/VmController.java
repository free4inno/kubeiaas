package kubeiaas.dbproxy.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.dbproxy.dao.VmDao;
import kubeiaas.dbproxy.table.HostTable;
import kubeiaas.dbproxy.table.VmTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
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

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1) {
        log.info("queryAllBySingleKey ==== start ====");
        Specification<VmTable> specification = (root, cq, cb) ->
                cb.and(cb.equal(root.get(key1), value1));
        List<VmTable> vmTableList = vmDao.findAll(specification);
        log.info("queryAllBySingleKey ==== end ====");
        return JSON.toJSONString(vmTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SAVE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String save(
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObjectStr) {
        log.info("save ==== start ====");
        VmTable vmTable = JSON.parseObject(vmObjectStr, VmTable.class);
        vmDao.saveAndFlush(vmTable);
        log.info("save ==== end ====");
        return JSON.toJSONString(vmTable);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_BY_UUID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public void deleteByUuid(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid) {
        log.info("deleteInstanceByUuid ==== start ==== vmUuid:" + vmUuid);
        vmDao.deleteByUuid(vmUuid);
        log.info("deleteInstanceByUuid ==== end ====");
    }
}
