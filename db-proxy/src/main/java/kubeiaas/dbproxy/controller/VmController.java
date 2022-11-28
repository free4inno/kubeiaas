package kubeiaas.dbproxy.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.common.enums.volume.VolumeUsageEnum;
import kubeiaas.dbproxy.dao.IpUsedDao;
import kubeiaas.dbproxy.dao.VmDao;
import kubeiaas.dbproxy.table.IpUsedTable;
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

    @Resource
    private IpUsedDao ipUsedDao;

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

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.FUZZY_QUERY_VM, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String fuzzyQueryVm(
            @RequestParam(value = RequestParamConstants.VALUE_1) String param,
            @RequestParam(value = RequestParamConstants.STATUS) String status,
            @RequestParam(value = RequestParamConstants.HOST_UUID) String hostUuid,
            @RequestParam(value = RequestParamConstants.IMAGE_UUID) String imageUuid) {
        log.info("fuzzyQueryDataVolumes ==== start ====");

        if (judgeParamIsNull(param) && judgeParamIsNull(status) && judgeParamIsNull(hostUuid) && judgeParamIsNull(imageUuid)){
            List<VmTable> vmTableList = vmDao.findAll();
            return JSON.toJSONString(vmTableList);
        }else {
            //判断查询是否有状态输入，无状态输入则直接模糊查询全部
            Specification<VmTable> specification = (root, cq, cb) ->{
                List<Predicate> predicates = new ArrayList<Predicate>();
                List<Predicate> param_predicates = new ArrayList<Predicate>();
                if (!judgeParamIsNull(param)){
                    List<String> vmUuidList = getVmUuidByIp(param);
                    log.info("___________________"+vmUuidList);
                    for (String vmUuid : vmUuidList) {
                        param_predicates.add(cb.equal(root.get("uuid"),vmUuid));
                    }
                    param_predicates.add(cb.like(root.get(RequestParamConstants.NAME), "%" + param + "%"));
                    param_predicates.add(cb.like(root.get(RequestParamConstants.DESCRIPTION), "%" + param + "%"));
                    predicates.add(cb.or(param_predicates.toArray(new Predicate[predicates.size()])));
                }
                if (!judgeParamIsNull(status)){
                    VmStatusEnum vmStatus = getVmStatus(status);
                    predicates.add(cb.equal(root.get(RequestParamConstants.STATUS), vmStatus));
                }
                if (!judgeParamIsNull(hostUuid)){
                    predicates.add(cb.equal(root.get("hostUuid"), hostUuid));
                }
                if (!judgeParamIsNull(imageUuid)){
                    predicates.add(cb.equal(root.get("imageUuid"), imageUuid));
                }
                return cq.where(predicates.toArray(new Predicate[predicates.size()])).getRestriction();
            };
            List<VmTable> vmTableList = vmDao.findAll(specification);
            return JSON.toJSONString(vmTableList);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.PAGE_FUZZY_QUERY_VM, produces = RequestMappingConstants.APP_JSON)
    @ResponseBody
    public String pageFuzzyQueryVm(
            @RequestParam(value = RequestParamConstants.VALUE_1) String param,
            @RequestParam(value = RequestParamConstants.STATUS) String status,
            @RequestParam(value = RequestParamConstants.HOST_UUID) String hostUuid,
            @RequestParam(value = RequestParamConstants.IMAGE_UUID) String imageUuid,
            @RequestParam(value = RequestParamConstants.PAGE_NUM) Integer pageNum,
            @RequestParam(value = RequestParamConstants.PAGE_SIZE) Integer pageSize) {
        log.info("pageFuzzyQueryVm ==== start ====");

        if (judgeParamIsNull(param) && judgeParamIsNull(status) && judgeParamIsNull(hostUuid) && judgeParamIsNull(imageUuid)){
            // 1. build pageable
            // (pageNum in Pageable is from 0-n, so we need to `pageNum - 1`)
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
            Page<VmTable> vmPage = vmDao.findAll(pageable);
            log.info("pageFuzzyQueryVm ==== end ====");
            return JSON.toJSONString(vmPage);
        }else {
            //判断查询是否有状态输入，无状态输入则直接模糊查询全部
            Specification<VmTable> specification = (root, cq, cb) ->{
                List<Predicate> predicates = new ArrayList<Predicate>();
                List<Predicate> param_predicates = new ArrayList<Predicate>();
                if (!judgeParamIsNull(param)){
                    List<String> vmUuidList = getVmUuidByIp(param);
                    log.info("___________________"+vmUuidList);
                    for (String vmUuid : vmUuidList) {
                        param_predicates.add(cb.equal(root.get("uuid"),vmUuid));
                    }
                    param_predicates.add(cb.like(root.get(RequestParamConstants.NAME), "%" + param + "%"));
                    param_predicates.add(cb.like(root.get(RequestParamConstants.DESCRIPTION), "%" + param + "%"));
                    predicates.add(cb.or(param_predicates.toArray(new Predicate[predicates.size()])));
                }
                if (!judgeParamIsNull(status)){
                    VmStatusEnum vmStatus = getVmStatus(status);
                    predicates.add(cb.equal(root.get(RequestParamConstants.STATUS), vmStatus));
                }
                if (!judgeParamIsNull(hostUuid)){
                    predicates.add(cb.equal(root.get("hostUuid"), hostUuid));
                }
                if (!judgeParamIsNull(imageUuid)){
                    predicates.add(cb.equal(root.get("imageUuid"), imageUuid));
                }
                return cq.where(predicates.toArray(new Predicate[predicates.size()])).getRestriction();
            };
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
            Page<VmTable> vmPage = vmDao.findAll(specification, pageable);
            log.info("pageFuzzyQueryVm ==== end ====");
            return JSON.toJSONString(vmPage);
        }
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

    private boolean judgeParamIsNull(String param){
        if (param == null || param.equals("") || param.equals("null")) {
            return true;
        }else{
            return false;
        }
    }

    private VmStatusEnum getVmStatus(String status){

        VmStatusEnum VmStatus ;
        if (status.equals("active")) {
            VmStatus = VmStatusEnum.ACTIVE;
        } else if (status.equals("stopped")) {
            VmStatus = VmStatusEnum.STOPPED;
        } else if (status.equals("suspended")) {
            VmStatus = VmStatusEnum.SUSPENDED;
        } else if (status.equals("error")) {
            VmStatus = VmStatusEnum.ERROR;
        } else {
            VmStatus = null;
        }
        return VmStatus;
    }

    private List<String> getVmUuidByIp(String param){
        List<String> vmUuidList = new ArrayList<String>();
        Specification<IpUsedTable> specification = (root, cq, cb) ->
                cb.and(cb.like(root.get("ip"), "%" + param + "%"));
        List<IpUsedTable> ipUsedTableList = ipUsedDao.findAll(specification);
        for (IpUsedTable ipUsedTable : ipUsedTableList) {
            vmUuidList.add(ipUsedTable.getInstanceUuid());
        }
        return vmUuidList;
    }
}
