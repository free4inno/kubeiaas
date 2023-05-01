package kubeiaas.dbproxy.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.bean.SpecConfigConstants;
import kubeiaas.common.enums.config.SpecTypeEnum;
import kubeiaas.dbproxy.dao.SpecConfigDao;
import kubeiaas.dbproxy.table.SpecConfigTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
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
@RequestMapping(value = RequestMappingConstants.SPEC_CONFIG)
public class SpecConfigController {

    @Resource
    private SpecConfigDao specConfigDao;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_TYPE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllByType(
            @RequestParam(value = RequestParamConstants.TYPE) SpecTypeEnum type) {
        log.info("queryAllByType ==== start ====");
        Specification<SpecConfigTable> specification = (root, cq, cb) ->
                cb.and(cb.equal(root.get(SpecConfigConstants.TYPE), type));
        List<SpecConfigTable> specConfigTableList = specConfigDao.findAll(specification);
        log.info("queryAllByType ==== end ====");
        return JSON.toJSONString(specConfigTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("queryAll == ");
        Sort s = Sort.by(Sort.Direction.ASC, SpecConfigConstants.TYPE);
        return JSON.toJSONString(specConfigDao.findAll(s));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SAVE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public synchronized String save(
            @RequestParam(value = RequestParamConstants.OBJECT) String objectStr) {
        log.info("save == ");
        SpecConfigTable table = JSON.parseObject(objectStr, SpecConfigTable.class);
        specConfigDao.saveAndFlush(table);
        return JSON.toJSONString(table);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_BY_ID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public synchronized void deleteById(
            @RequestParam(value = RequestParamConstants.ID) Integer id) {
        log.info("delete == id: " + id);
        specConfigDao.deleteById(id);
    }

}
