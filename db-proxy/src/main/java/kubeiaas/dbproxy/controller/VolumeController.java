package kubeiaas.dbproxy.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.common.enums.volume.VolumeUsageEnum;
import kubeiaas.dbproxy.dao.VolumeDao;
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
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.VOLUME)
public class VolumeController {
    @Resource
    private VolumeDao volumeDao;

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

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.FUZZY_QUERY_DATA_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String fuzzyQueryDataVolumes(
            @RequestParam(value = RequestParamConstants.VALUE_1) String param,
            @RequestParam(value = RequestParamConstants.VALUE_2) String value2) {
        log.info("fuzzyQueryDataVolumes ==== start ====");

        VolumeStatusEnum status ;
        //判断查询是否有状态输入，无状态输入则直接模糊查询全部
        boolean paramFlag = true;
        boolean statusFlag = true;
        if (param == null || param.equals("") || param.equals("null")) {
            paramFlag = false;
        }
        if (value2.equals("attached")) {
            status = VolumeStatusEnum.ATTACHED;
        } else if (value2.equals("available")) {
            status = VolumeStatusEnum.AVAILABLE;
        } else if (value2.equals("creating")) {
            status = VolumeStatusEnum.CREATING;
        } else if (value2.equals("error")) {
            status = VolumeStatusEnum.ERROR;
        } else {
            status = null;
            statusFlag = false;
        }
        Specification<VolumeTable> specification;
        if (paramFlag && statusFlag){
            specification = (root, cq, cb) ->
                    cb.and(cb.or(
                                    cb.like(root.get(RequestParamConstants.NAME), "%" + param + "%"),
                                    cb.like(root.get(RequestParamConstants.DESCRIPTION), "%" + param + "%")
                            ),
                            cb.equal(root.get(RequestParamConstants.STATUS), status),
                            cb.equal(root.get(VolumeConstants.USAGE_TYPE), VolumeUsageEnum.DATA)
                    );
        }else if (paramFlag){
            specification = (root, cq, cb) ->
                    cb.and(cb.or(
                                cb.like(root.get(RequestParamConstants.NAME), "%" + param + "%"),
                                cb.like(root.get(RequestParamConstants.DESCRIPTION), "%" + param + "%")
                            ),
                            cb.equal(root.get(VolumeConstants.USAGE_TYPE), VolumeUsageEnum.DATA)
                            );
        }else if (statusFlag){
            specification = (root, cq, cb) ->
                    cb.and(cb.equal(root.get(RequestParamConstants.STATUS), status),
                            cb.equal(root.get(VolumeConstants.USAGE_TYPE), VolumeUsageEnum.DATA));
        }else {
            specification = (root, cq, cb) ->
                    cb.and(cb.equal(root.get(VolumeConstants.USAGE_TYPE), VolumeUsageEnum.DATA));
        }
        List<VolumeTable> volumeTableList = volumeDao.findAll(specification);
        log.info("fuzzyQueryDataVolumes ==== end ====");
        return JSON.toJSONString(volumeTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.PAGE_FUZZY_QUERY_DATA_VOLUME, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String pageFuzzyQueryDataVolume(
            @RequestParam(value = RequestParamConstants.VALUE_1) String param,
            @RequestParam(value = RequestParamConstants.VALUE_2) String value2,
            @RequestParam(value = RequestParamConstants.PAGE_NUM) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) Integer pageSize) {
        log.info("pageFuzzyQueryDataVolume ==== start ====");
        VolumeStatusEnum status ;
        //判断查询是否有状态输入，无状态输入则直接模糊查询全部
        boolean paramFlag = true;
        boolean statusFlag = true;
        if (param == null || param.equals("") || param.equals("null")) {
            paramFlag = false;
        }
        if (value2.equals("attached")) {
            status = VolumeStatusEnum.ATTACHED;
        } else if (value2.equals("available")) {
            status = VolumeStatusEnum.AVAILABLE;
        } else if (value2.equals("creating")) {
            status = VolumeStatusEnum.CREATING;
        } else if (value2.equals("error")) {
            status = VolumeStatusEnum.ERROR;
        } else {
            status = null;
            statusFlag = false;
        }
        Specification<VolumeTable> specification;
        if (paramFlag && statusFlag){
            specification = (root, cq, cb) ->
                    cb.and(cb.or(
                                    cb.like(root.get(RequestParamConstants.NAME), "%" + param + "%"),
                                    cb.like(root.get(RequestParamConstants.DESCRIPTION), "%" + param + "%")
                            ),
                            cb.equal(root.get(RequestParamConstants.STATUS), status),
                            cb.equal(root.get(VolumeConstants.USAGE_TYPE), VolumeUsageEnum.DATA)
                    );
        }else if (paramFlag){
            specification = (root, cq, cb) ->
                    cb.and(cb.or(
                                    cb.like(root.get(RequestParamConstants.NAME), "%" + param + "%"),
                                    cb.like(root.get(RequestParamConstants.DESCRIPTION), "%" + param + "%")
                            ),
                            cb.equal(root.get(VolumeConstants.USAGE_TYPE), VolumeUsageEnum.DATA)
                    );
        }else if (statusFlag){
            specification = (root, cq, cb) ->
                    cb.and(cb.equal(root.get(RequestParamConstants.STATUS), status),
                            cb.equal(root.get(VolumeConstants.USAGE_TYPE), VolumeUsageEnum.DATA));
        }else {
            specification = (root, cq, cb) ->
                    cb.and(cb.equal(root.get(VolumeConstants.USAGE_TYPE), VolumeUsageEnum.DATA));
        }
        // 1. build pageable
        // (pageNum in Pageable is from 0-n, so we need to `pageNum - 1`)
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        // 2. do query
        Page<VolumeTable> volumePage = volumeDao.findAll(specification, pageable);
        log.info("pageFuzzyQueryDataVolume ==== end ====");
        return JSON.toJSONString(volumePage);
    }

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
}
