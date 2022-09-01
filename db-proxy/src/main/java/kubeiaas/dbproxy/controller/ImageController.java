package kubeiaas.dbproxy.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.dbproxy.dao.ImageDao;
import kubeiaas.dbproxy.dao.VmDao;
import kubeiaas.dbproxy.table.ImageTable;
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
@RequestMapping(value = RequestMappingConstants.IMAGE)
public class ImageController {
    @Resource
    private ImageDao imageDao;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("queryAll ==== start ====");
        List<ImageTable> imageTableList = imageDao.findAll();
        log.info("queryAll ==== end ====");
        return JSON.toJSONString(imageTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1) {
        log.info("queryAllBySingleKey ==== start ====");
        Specification<ImageTable> specification = (root, cq, cb) ->
                cb.and(cb.equal(root.get(key1), value1));
        List<ImageTable> imageTableList = imageDao.findAll(specification);
        log.info("queryAllBySingleKey ==== end ====");
        return JSON.toJSONString(imageTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SAVE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public void save(
            @RequestParam(value = RequestParamConstants.IMAGE_OBJECT) String imageObjectStr) {
        log.info("save ==== start ====");
        ImageTable imageTable = JSON.parseObject(imageObjectStr, ImageTable.class);
        imageDao.saveAndFlush(imageTable);
        log.info("save ==== end ====");
    }
}
