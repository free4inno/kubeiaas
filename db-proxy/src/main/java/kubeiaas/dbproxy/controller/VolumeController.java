package kubeiaas.dbproxy.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.common.enums.volume.VolumeUsageEnum;
import kubeiaas.common.utils.EnumUtils;
import kubeiaas.dbproxy.dao.VmDao;
import kubeiaas.dbproxy.dao.VolumeDao;
import kubeiaas.dbproxy.table.VmTable;
import kubeiaas.dbproxy.table.VolumeTable;
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

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.VOLUME)
public class VolumeController {

    @Resource
    private VolumeDao volumeDao;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_DATA_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllDataVolume() {
        log.info("queryAllDataVolume ==== start ====");
        Specification<VolumeTable> specification = (root, cq, cb) ->
                cb.and(cb.equal(root.get(VolumeConstants.USAGE_TYPE), VolumeUsageEnum.DATA));
        List<VolumeTable> volumeTableList = volumeDao.findAll(specification);
        log.info("queryAllDataVolume ==== end ====");
        return JSON.toJSONString(volumeTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.PAGE_QUERY_ALL_DATA_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String pageQueryAllDataVolume(
            @RequestParam(value = RequestParamConstants.PAGE_NUM) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) Integer pageSize) {
        log.info("pageQueryAllDataVolume ==== start ====");
        Specification<VolumeTable> specification = (root, cq, cb) ->
                cb.and(cb.equal(root.get(VolumeConstants.USAGE_TYPE), VolumeUsageEnum.DATA));
        // 1. build pageable
        // (pageNum in Pageable is from 0-n, so we need to `pageNum - 1`)
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        // 2. do query
        Page<VolumeTable> volumePage = volumeDao.findAll(specification, pageable);
        log.info("pageQueryAllDataVolume ==== end ====");
        return JSON.toJSONString(volumePage);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.FUZZY_QUERY_DATA_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String fuzzyQueryDataVolume(
            @RequestParam(value = RequestParamConstants.KEYWORDS) String keywords,
            @RequestParam(value = RequestParamConstants.STATUS) String status,
            @RequestParam(value = RequestParamConstants.PAGE_NUM) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) Integer pageSize) {
        log.info("fuzzyQueryDataVolume ==== start ====");
        // 1. build pageable
        //    - pageNum in Pageable is from 0-n, so we need to `pageNum - 1`
        // 2. build specification
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Specification<VolumeTable> specification;
        if (isNullParam(keywords) && isNullParam(status)) {
            specification = (root, cq, cb) ->
                    cb.and(cb.equal(root.get(VolumeConstants.USAGE_TYPE), VolumeUsageEnum.DATA));
        } else {
            specification = buildFuzzySpec(keywords, status);
        }
        Page<VolumeTable> volumePage = volumeDao.findAll(specification, pageable);
        log.info("fuzzyQueryDataVolume ==== end ====");
        return JSON.toJSONString(volumePage);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_BY_SINGLE_KEY, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String queryAllBySingleKey(
            @RequestParam(value = RequestParamConstants.KEY_1) String key1,
            @RequestParam(value = RequestParamConstants.VALUE_1) String value1) {
        log.info("queryAllBySingleKey ==== start ====");
        Specification<VolumeTable> specification = (root, cq, cb) ->
                cb.and(cb.equal(root.get(key1), value1));
        List<VolumeTable> volumeTableList = volumeDao.findAll(specification);
        log.info("queryAllBySingleKey ==== end ====");
        return JSON.toJSONString(volumeTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SAVE, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String save(
            @RequestParam(value = RequestParamConstants.VOLUME_OBJECT) String volumeObjectStr) {
        log.info("save ==== start ====");
        VolumeTable volumeTable = JSON.parseObject(volumeObjectStr, VolumeTable.class);
        volumeDao.saveAndFlush(volumeTable);
        log.info("save ==== end ====");
        return JSON.toJSONString(volumeTable);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_BY_UUID, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public void deleteByUuid(
            @RequestParam(value = RequestParamConstants.VOLUME_UUID) String volumeUuid) {
        log.info("deleteVolumeByUuid ==== start ==== uuid:" + volumeUuid);
        volumeDao.deleteByUuid(volumeUuid);
        log.info("deleteVolumeByUuid ==== end ====");
    }

    // =================================================================================================================

    private Specification<VolumeTable> buildFuzzySpec(String keywords, String status) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            List<Predicate> keywords_predicates = new ArrayList<>();
            // 0. DATA_VOLUME
            predicates.add(cb.equal(root.get(VolumeConstants.USAGE_TYPE), VolumeUsageEnum.DATA));
            // 1. keywords
            if (!isNullParam(keywords)){
                // 1.1. vm name
                List<String> vmUuidList = getVmUuidByName(keywords);
                for (String vmUuid : vmUuidList) {
                    keywords_predicates.add(cb.equal(root.get(VolumeConstants.INSTANCE_UUID), vmUuid));
                }
                // 1.2. data volume name
                keywords_predicates.add(cb.like(root.get(VolumeConstants.NAME), "%" + keywords + "%"));
                // 1.3. description
                keywords_predicates.add(cb.like(root.get(VolumeConstants.DESCRIPTION), "%" + keywords + "%"));
                predicates.add(cb.or(keywords_predicates.toArray(new Predicate[0])));
            }
            // 2. status
            if (!isNullParam(status)){
                VolumeStatusEnum volumeStatus = EnumUtils.getEnumFromString(VolumeStatusEnum.class, status);
                predicates.add(cb.equal(root.get(VolumeConstants.STATUS), volumeStatus));
            }
            int size = predicates.size();
            return cq.where(predicates.toArray(new Predicate[size])).getRestriction();
        };
    }

    private boolean isNullParam(String param){
        return param == null || param.equals("") || param.equals("null");
    }

    @Resource
    private VmDao vmDao;

    private List<String> getVmUuidByName(String param){
        List<String> vmUuidList = new ArrayList<>();
        Specification<VmTable> specification = (root, cq, cb) ->
                cb.and(cb.like(root.get("name"), "%" + param + "%"));
        List<VmTable> vmTableList = vmDao.findAll(specification);
        for (VmTable vmTable : vmTableList) {
            vmUuidList.add(vmTable.getUuid());
        }
        return vmUuidList;
    }
}
