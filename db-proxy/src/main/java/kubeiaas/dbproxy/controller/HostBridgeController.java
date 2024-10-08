package kubeiaas.dbproxy.controller;

import com.alibaba.fastjson2.JSON;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.dbproxy.dao.HostBridgeDao;
import kubeiaas.dbproxy.table.HostBridgeTable;
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
@RequestMapping(value = RequestMappingConstants.HOST_BRIDGE)
public class HostBridgeController {
    @Resource
    private HostBridgeDao hostBridgeDao;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("-- start -- queryAll");
        List<HostBridgeTable> hostBridgeTableList = hostBridgeDao.findAll();
        log.info("-- end -- queryAll");
        return JSON.toJSONString(hostBridgeTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1) {
        log.info("-- start -- queryAllBySingleKey -- key1: {}, value1: {}", key1, value1);
        Specification<HostBridgeTable> specification = (root, cq, cb) ->
                cb.and(cb.equal(root.get(key1), value1));
        List<HostBridgeTable> hostBridgeTableList = hostBridgeDao.findAll(specification);
        log.info("-- end -- queryAllBySingleKey");
        return JSON.toJSONString(hostBridgeTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_DOUBLE_KEY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllByDoubleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1,
            @RequestParam(value = RequestParamConstants.KEY_2) String key2,
            @RequestParam(value = RequestParamConstants.VALUE_2) String value2) {
        log.info("-- start -- queryAllByDoubleKey -- key1: {}, value1: {}, key2: {}, value2: {}", key1, value1, key2, value2);
        Specification<HostBridgeTable> specification = (root, cq, cb) ->
                cb.and(cb.equal(root.get(key1), value1), cb.equal(root.get(key2), value2));
        List<HostBridgeTable> hostBridgeTableList = hostBridgeDao.findAll(specification);
        log.info("-- end -- queryAllByDoubleKey");
        return JSON.toJSONString(hostBridgeTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SAVE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public synchronized String save(
            @RequestParam(value = RequestParamConstants.HOST_BRIDGE_OBJECT) String hostBridgeObjectStr) {
        log.info("-- start -- save -- hostBridgeObjectStr: {}", hostBridgeObjectStr);
        HostBridgeTable hostBridgeTable = JSON.parseObject(hostBridgeObjectStr, HostBridgeTable.class);
        hostBridgeDao.saveAndFlush(hostBridgeTable);
        log.info("-- end -- save");
        return JSON.toJSONString(hostBridgeTable);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_BY_ID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public synchronized void deleteById(
            @RequestParam(value = RequestParamConstants.HOST_BRIDGE_ID) Integer hostBridgeId) {
        log.info("-- start -- deleteById -- hostBridgeId: {}", hostBridgeId);
        hostBridgeDao.deleteById(hostBridgeId);
        log.info("-- end -- deleteById");
    }
}
