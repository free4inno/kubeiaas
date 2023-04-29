package kubeiaas.dbproxy.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.dbproxy.dao.IpUsedDao;
import kubeiaas.dbproxy.table.IpUsedTable;
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
@RequestMapping(value = RequestMappingConstants.IP_USED)
public class IpUsedController {
    @Resource
    private IpUsedDao ipUsedDao;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1) {
        log.info("queryAllBySingleKey ==== start ====");
        Specification<IpUsedTable> specification = (root, cq, cb) ->
                cb.and(cb.equal(root.get(key1), value1));
        List<IpUsedTable> ipUsedTableList = ipUsedDao.findAll(specification);
        log.info("queryAllBySingleKey ==== end ====");
        return JSON.toJSONString(ipUsedTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SAVE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public synchronized String save(
            @RequestParam(value = RequestParamConstants.IP_USED_OBJECT) String ipUsedObjectStr) {
        log.info("save ==== start ====");
        IpUsedTable ipUsedTable = JSON.parseObject(ipUsedObjectStr, IpUsedTable.class);
        ipUsedDao.saveAndFlush(ipUsedTable);
        log.info("save ==== end ====");
        return JSON.toJSONString(ipUsedTable);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_ALL_BY_UUID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public synchronized void deleteByVmUuid(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid) {
        log.info("deleteUsedIpsByInstanceUuid ==== start ==== vmUuid:" + vmUuid);
        ipUsedDao.deleteAllByInstanceUuid(vmUuid);
        log.info("deleteUsedIpsByInstanceUuid ==== end ==== ");
    }

}
