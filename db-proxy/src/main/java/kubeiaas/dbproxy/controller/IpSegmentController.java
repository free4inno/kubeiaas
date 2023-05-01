package kubeiaas.dbproxy.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.bean.IpSegmentConstants;
import kubeiaas.common.enums.network.IpTypeEnum;
import kubeiaas.common.utils.EnumUtils;
import kubeiaas.dbproxy.dao.IpSegmentDao;
import kubeiaas.dbproxy.table.IpSegmentTable;
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
@RequestMapping(value = RequestMappingConstants.IP_SEGMENT)
public class IpSegmentController {
    @Resource
    private IpSegmentDao ipSegmentDao;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("queryAll ==== start ====");
        List<IpSegmentTable> ipSegmentTableList = ipSegmentDao.findAll();
        log.info("queryAll ==== end ====");
        return JSON.toJSONString(ipSegmentTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1) {
        log.info("queryAllBySingleKey ==== start ====");
        Specification<IpSegmentTable> specification = (root, cq, cb) ->
                cb.and(cb.equal(root.get(key1), value1));
        List<IpSegmentTable> ipSegmentTableList = ipSegmentDao.findAll(specification);
        log.info("queryAllBySingleKey ==== end ====");
        return JSON.toJSONString(ipSegmentTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_DOUBLE_KEY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllByDoubleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1,
            @RequestParam(value = RequestParamConstants.KEY_2) String key2,
            @RequestParam(value = RequestParamConstants.VALUE_2) String value2) {
        log.info("queryAllByDoubleKey ==== start ====");
        Specification<IpSegmentTable> specification;
        if (key2.equals(IpSegmentConstants.TYPE)) {
            IpTypeEnum type = EnumUtils.getEnumFromString(IpTypeEnum.class, value2);
            specification = (root, cq, cb) ->
                    cb.and(cb.equal(root.get(key1), value1), cb.equal(root.get(key2), type));
        } else {
            specification = (root, cq, cb) ->
                    cb.and(cb.equal(root.get(key1), value1), cb.equal(root.get(key2), value2));
        }
        List<IpSegmentTable> ipSegmentTableList = ipSegmentDao.findAll(specification);
        log.info("queryAllByDoubleKey ==== end ====");
        return JSON.toJSONString(ipSegmentTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SAVE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public synchronized String save(
            @RequestParam(value = RequestParamConstants.IP_SEGMENT_OBJECT) String ipSegmentObjectStr) {
        log.info("save ==== start ====");
        IpSegmentTable ipSegmentTable = JSON.parseObject(ipSegmentObjectStr, IpSegmentTable.class);
        ipSegmentDao.saveAndFlush(ipSegmentTable);
        log.info("save ==== end ====");
        return JSON.toJSONString(ipSegmentTable);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_BY_ID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public synchronized void deleteById(
            @RequestParam(value = RequestParamConstants.IP_SEGMENT_ID) Integer ipSegmentId) {
        log.info("deleteById ==== start ==== id:" + ipSegmentId);
        ipSegmentDao.deleteById(ipSegmentId);
        log.info("deleteById ==== end ====");
    }
}
