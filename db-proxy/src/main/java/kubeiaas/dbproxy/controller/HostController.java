package kubeiaas.dbproxy.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.dbproxy.dao.HostDao;
import kubeiaas.dbproxy.table.HostTable;
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
@RequestMapping(value = RequestMappingConstants.HOST)
public class HostController {
    @Resource
    private HostDao hostDao;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("-- start -- queryAll");
        List<HostTable> hostTableList = hostDao.findAll();
        log.info("-- end -- queryAll");
        return JSON.toJSONString(hostTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1) {
        log.info("-- start -- queryAllBySingleKey -- key1: {}, value1: {}", key1, value1);
        Specification<HostTable> specification = (root, cq, cb) ->
                cb.and(cb.equal(root.get(key1), value1));
        List<HostTable> hostTableList = hostDao.findAll(specification);
        log.info("-- end -- queryAllBySingleKey");
        return JSON.toJSONString(hostTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_LIKE_BY_SINGLE_KEY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllLikeBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1) {
        log.info("-- start -- queryAllLikeBySingleKey -- key1: {}, value1: {}", key1, value1);
        Specification<HostTable> specification = (root, cq, cb) ->
                cb.and(cb.like(root.get(key1), "%" + value1 + "%"));
        List<HostTable> hostTableList = hostDao.findAll(specification);
        log.info("-- end -- queryAllLikeBySingleKey");
        return JSON.toJSONString(hostTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SAVE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public synchronized String save(
            @RequestParam(value = RequestParamConstants.HOST_OBJECT) String hostObjectStr) {
        log.info("-- start -- save -- hostObjectStr: {}", hostObjectStr);
        HostTable hostTable = JSON.parseObject(hostObjectStr, HostTable.class);
        hostDao.saveAndFlush(hostTable);
        log.info("-- end -- save");
        return JSON.toJSONString(hostTable);
    }
}
