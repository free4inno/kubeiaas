package kubeiaas.dbproxy.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.dbproxy.dao.DeviceDao;
import kubeiaas.dbproxy.table.DeviceTable;
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
@RequestMapping(value = RequestMappingConstants.DEVICE)
public class DeviceController {
    @Resource
    private DeviceDao deviceDao;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("queryAll ==== start ====");
        List<DeviceTable> deviceTableList = deviceDao.findAll();
        log.info("queryAll ==== end ====");
        return JSON.toJSONString(deviceTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1) {
        log.info("queryAllBySingleKey ==== start ====");
        Specification<DeviceTable> specification = (root, cq, cb) ->
                cb.and(cb.equal(root.get(key1), value1));
        List<DeviceTable> deviceTableList = deviceDao.findAll(specification);
        log.info("queryAllBySingleKey ==== end ====");
        return JSON.toJSONString(deviceTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SAVE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public synchronized String save(
            @RequestParam(value = RequestParamConstants.DEVICE_OBJECT) String deviceObjectStr) {
        log.info("save ==== start ====");
        DeviceTable deviceTable = JSON.parseObject(deviceObjectStr, DeviceTable.class);
        deviceDao.saveAndFlush(deviceTable);
        log.info("save ==== end ====");
        return JSON.toJSONString(deviceTable);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_BY_ID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public synchronized void deleteById(
            @RequestParam(value = RequestParamConstants.ID) Integer id) {
        log.info("deleteById ==== start ==== id:" + id);
        deviceDao.deleteById(id);
        log.info("deleteById ==== end ====");
    }
}
