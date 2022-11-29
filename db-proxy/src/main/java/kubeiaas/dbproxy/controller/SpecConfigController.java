package kubeiaas.dbproxy.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.bean.SpecConfigConstants;
import kubeiaas.common.enums.config.SpecTypeEnum;
import kubeiaas.dbproxy.dao.SpecConfigDao;
import kubeiaas.dbproxy.table.SpecConfigTable;
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

}
