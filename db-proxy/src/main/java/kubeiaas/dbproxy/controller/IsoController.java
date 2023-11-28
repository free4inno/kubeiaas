package kubeiaas.dbproxy.controller;


import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Iso;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.dbproxy.dao.IsoDao;
import kubeiaas.dbproxy.table.IsoTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.ISO)
public class IsoController {
    @Resource
    private IsoDao isoDao;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_BY_UUID)
    @ResponseBody
    public String isoQueryByUuid(
            @RequestParam(value = RequestParamConstants.ISO_UUID) String isoUuid) {
        log.info("isoQueryByUuid ==== start ====");
        log.info("isoUuid: {}", isoUuid);
        Iso iso = isoDao.findByUuid(isoUuid);
        log.info("isoQueryByUuid ==== end ====");
        return JSON.toJSONString(iso);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL)
    @ResponseBody
    public String isoQueryAll() {
        log.info("isoQueryAll ==== start ====");
        Iterable<IsoTable> isoTableIterable = isoDao.findAll();
        log.info("isoQueryAll ==== end ====");
        return JSON.toJSONString(isoTableIterable);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SAVE)
    @ResponseBody
    public String isoSave(
            @RequestParam(value = RequestParamConstants.ISO_OBJECT) String isoObjectStr) {
        log.info("isoSave ==== start ====");
        log.info("isoObjectStr: {}", isoObjectStr);
        IsoTable isoTable = JSON.parseObject(isoObjectStr, IsoTable.class);
        IsoTable savedIsoTable = isoDao.saveAndFlush(isoTable);
        log.info("isoSave ==== end ====");
        return JSON.toJSONString(savedIsoTable);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE)
    @ResponseBody
    public void deleteIsoTable() {
        isoDao.deleteAll();
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_BY_UUID)
    @ResponseBody
    public void deleteIsoByUuid(
            @RequestParam(value = RequestParamConstants.ISO_UUID) String isoUuid) {
        isoDao.deleteByUuid(isoUuid);
    }
}
