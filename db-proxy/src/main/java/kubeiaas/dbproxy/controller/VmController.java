package kubeiaas.dbproxy.controller;

import com.alibaba.fastjson.JSONObject;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.dbproxy.dao.VmDao;
import kubeiaas.dbproxy.table.VmTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.VM)
public class VmController {
    @Resource
    private VmDao vmDao;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = {"application/json", "application/xml"})
    @ResponseBody
    public String queryAll() {
        log.info("queryAll ==== start ====");
        List<VmTable> vmTableList = vmDao.findAll();
        String res = JSONObject.toJSONString(vmTableList);
//        JSONObject jsonObject = new JSONObject((Map<String, Object>) vmTableList);
        log.info("queryAll ==== end ====");
        return res;
    }
}
