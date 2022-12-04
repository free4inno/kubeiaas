package kubeiaas.dbproxy.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.bean.VmConstants;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.utils.EnumUtils;
import kubeiaas.dbproxy.dao.IpUsedDao;
import kubeiaas.dbproxy.dao.VmDao;
import kubeiaas.dbproxy.table.IpUsedTable;
import kubeiaas.dbproxy.table.VmTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.persistence.criteria.Predicate;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.VM)
public class VmController {

    @Resource
    private VmDao vmDao;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAll() {
        log.info("queryAll ==== start ====");
        List<VmTable> vmTableList = vmDao.findAll();
        log.info("queryAll ==== end ====");
        return JSON.toJSONString(vmTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1) {
        log.info("queryAllBySingleKey ==== start ====");
        Specification<VmTable> specification = (root, cq, cb) ->
                cb.and(cb.equal(root.get(key1), value1));
        List<VmTable> vmTableList = vmDao.findAll(specification);
        log.info("queryAllBySingleKey ==== end ====");
        return JSON.toJSONString(vmTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.PAGE_QUERY_ALL, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String pageQueryAll(
            @RequestParam(value = RequestParamConstants.PAGE_NUM) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) Integer pageSize) {
        log.info("pageQueryAll ==== start ====");
        // 1. build pageable
        // (pageNum in Pageable is from 0-n, so we need to `pageNum - 1`)
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        // 2. do query
        Page<VmTable> vmPage = vmDao.findAll(pageable);
        log.info("pageQueryAll ==== end ====");
        return JSON.toJSONString(vmPage);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.FUZZY_QUERY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String fuzzyQuery(
            @RequestParam(value = RequestParamConstants.KEYWORDS) String keywords,
            @RequestParam(value = RequestParamConstants.STATUS) String status,
            @RequestParam(value = RequestParamConstants.HOST_UUID) String hostUuid,
            @RequestParam(value = RequestParamConstants.IMAGE_UUID) String imageUuid,
            @RequestParam(value = RequestParamConstants.PAGE_NUM) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) Integer pageSize) {
        log.info("pageQueryFuzzy ==== start ====");
        // 1. build pageable
        //    - pageNum in Pageable is from 0-n, so we need to `pageNum - 1`
        // 2. build specification
        Page<VmTable> vmPage;
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        if (isNullParam(keywords) && isNullParam(status) && isNullParam(hostUuid) && isNullParam(imageUuid)){
            vmPage = vmDao.findAll(pageable);
        } else {
            Specification<VmTable> specification = buildFuzzySpec(keywords, status, hostUuid, imageUuid, false);
            vmPage = vmDao.findAll(specification, pageable);
        }
        log.info("pageQueryFuzzy ==== end ====");
        return JSON.toJSONString(vmPage);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.FUZZY_QUERY_ATTACH, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String fuzzyQueryAttach(
            @RequestParam(value = RequestParamConstants.KEYWORDS) String keywords,
            @RequestParam(value = RequestParamConstants.PAGE_NUM) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) Integer pageSize) {
        log.info("fuzzyQueryAttach ==== start ====");
        // 1. build pageable
        //    - pageNum in Pageable is from 0-n, so we need to `pageNum - 1`
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        // 2. build specification
        Specification<VmTable> specification = buildFuzzySpec(keywords, null, null, null, true);
        // 3. do query
        Page<VmTable> vmPage = vmDao.findAll(specification, pageable);
        log.info("fuzzyQueryAttach ==== end ====");
        return JSON.toJSONString(vmPage);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SAVE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String save(
            @RequestParam(value = RequestParamConstants.VM_OBJECT) String vmObjectStr) {
        log.info("save ==== start ====");
        VmTable vmTable = JSON.parseObject(vmObjectStr, VmTable.class);
        vmDao.saveAndFlush(vmTable);
        log.info("save ==== end ====");
        return JSON.toJSONString(vmTable);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_BY_UUID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public void deleteByUuid(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid) {
        log.info("deleteInstanceByUuid ==== start ==== vmUuid:" + vmUuid);
        vmDao.deleteByUuid(vmUuid);
        log.info("deleteInstanceByUuid ==== end ====");
    }

    // =================================================================================================================

    private Specification<VmTable> buildFuzzySpec(String keywords, String status, String hostUuid, String imageUuid, boolean queryAttach) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            List<Predicate> keywords_predicates = new ArrayList<>();
            // 1. keywords
            if (!isNullParam(keywords)){
                // 1.1. ip
                List<String> vmUuidList = getVmUuidByIp(keywords);
                for (String vmUuid : vmUuidList) {
                    keywords_predicates.add(cb.equal(root.get(VmConstants.UUID),vmUuid));
                }
                // 1.2. name
                keywords_predicates.add(cb.like(root.get(VmConstants.NAME), "%" + keywords + "%"));
                // 1.3. description
                keywords_predicates.add(cb.like(root.get(VmConstants.DESCRIPTION), "%" + keywords + "%"));
                predicates.add(cb.or(keywords_predicates.toArray(new Predicate[0])));
            }
            // 2. status
            if (!isNullParam(status) && !queryAttach){
                // 2.1. query status
                VmStatusEnum vmStatus = EnumUtils.getEnumFromString(VmStatusEnum.class, status);
                predicates.add(cb.equal(root.get(VmConstants.STATUS), vmStatus));
            }
            if (queryAttach) {
                // 2.2. query attach
                predicates.add(
                    cb.or(
                        cb.equal(root.get(VmConstants.STATUS), VmStatusEnum.ACTIVE),
                        cb.equal(root.get(VmConstants.STATUS), VmStatusEnum.STOPPED),
                        cb.equal(root.get(VmConstants.STATUS), VmStatusEnum.SUSPENDED)
                    )
                );
            }
            // 4. host
            if (!isNullParam(hostUuid)){
                predicates.add(cb.equal(root.get(VmConstants.HOST_UUID), hostUuid));
            }
            // 5. image
            if (!isNullParam(imageUuid)){
                predicates.add(cb.equal(root.get(VmConstants.IMAGE_UUID), imageUuid));
            }
            int size = predicates.size();
            return cq.where(predicates.toArray(new Predicate[size])).getRestriction();
        };
    }

    private boolean isNullParam(String param){
        return param == null || param.equals("") || param.equals("null");
    }

    @Resource
    private IpUsedDao ipUsedDao;

    private List<String> getVmUuidByIp(String param){
        List<String> vmUuidList = new ArrayList<>();
        Specification<IpUsedTable> specification = (root, cq, cb) ->
                cb.and(cb.like(root.get("ip"), "%" + param + "%"));
        List<IpUsedTable> ipUsedTableList = ipUsedDao.findAll(specification);
        for (IpUsedTable ipUsedTable : ipUsedTableList) {
            vmUuidList.add(ipUsedTable.getInstanceUuid());
        }
        return vmUuidList;
    }
}
