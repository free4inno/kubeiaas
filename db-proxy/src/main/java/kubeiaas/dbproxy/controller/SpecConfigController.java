package kubeiaas.dbproxy.controller;

import com.alibaba.fastjson2.JSON;
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
        log.info("-- start -- queryAllByType -- type: {}", type);
        Specification<SpecConfigTable> specification = (root, cq, cb) ->
                cb.and(cb.equal(root.get(SpecConfigConstants.TYPE), type));
        List<SpecConfigTable> specConfigTableList = specConfigDao.findAll(specification);
        log.info("-- end -- queryAllByType");
        return JSON.toJSONString(specConfigTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("-- start -- queryAll");
        Sort s = Sort.by(Sort.Direction.ASC, SpecConfigConstants.TYPE);
        log.info("-- end -- queryAll");
        return JSON.toJSONString(specConfigDao.findAll(s));
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SAVE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public synchronized String save(
            @RequestParam(value = RequestParamConstants.OBJECT) String objectStr) {
        log.info("-- start -- save -- objectStr: {}", objectStr);
        SpecConfigTable table = JSON.parseObject(objectStr, SpecConfigTable.class);
        specConfigDao.saveAndFlush(table);
        log.info("-- end -- save");
        return JSON.toJSONString(table);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_BY_ID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public synchronized void deleteById(
            @RequestParam(value = RequestParamConstants.ID) Integer id) {
        log.info("-- start -- deleteById -- id: {}", id);
        specConfigDao.deleteById(id);
        log.info("-- end -- deleteById");
    }

}
