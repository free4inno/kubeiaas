package kubeiaas.dbproxy.controller;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Mount;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.dbproxy.dao.MountDao;
import kubeiaas.dbproxy.table.MountTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = RequestMappingConstants.MOUNT)
public class MountController {
    @Resource
    private MountDao mountDao;

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_ALL_MOUNT_POINT_BY_VM_UUID)
    @ResponseBody
    public String queryAllMountPointsByVmUuid(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid) {
        log.info("queryAllMountPointsByVmUuid ==== start ====");
        List<String> mountPoints = mountDao.findMountPointsByVmUuid(vmUuid);
        log.info("queryAllMountPointsByVmUuid ==== end ====");
        return JSON.toJSONString(mountPoints);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.QUERY_MOUNTS_BY_VM_UUID_AND_ISO_UUID)
    @ResponseBody
    public String queryMountsByVmUuidAndIsoUuid(
            @RequestParam(value = RequestParamConstants.VM_UUID) String vmUuid,
            @RequestParam(value = RequestParamConstants.ISO_UUID) String isoUuid) {
        log.info("queryMountsByVmUuidAndIsoUuid ==== start ====");
        List<MountTable> mountTableList = mountDao.findByVmUuidAndIsoUuid(vmUuid, isoUuid);
        log.info("queryMountsByVmUuidAndIsoUuid ==== end ====");
        return JSON.toJSONString(mountTableList);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.SAVE)
    @ResponseBody
    public synchronized String mountSave(
            @RequestParam(value = RequestParamConstants.MOUNT_OBJECT) String mountObjectStr) {
        log.info("mountSave ==== start ====");
        log.info("mountObjectStr: {}", mountObjectStr);
        MountTable mountTable = JSON.parseObject(mountObjectStr, MountTable.class);
        mountTable = mountDao.saveAndFlush(mountTable);
        log.info("mountTable: {}", mountTable);
        log.info("mountSave ==== end ====");
        return JSON.toJSONString(mountTable);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE_BY_ID)
    @ResponseBody
    public synchronized String mountDeleteById(
            @RequestParam(value = RequestParamConstants.ID) Integer id) {
        log.info("mountDeleteById ==== start ====");
        log.info("id: {}", id);
        mountDao.deleteById(id);
        log.info("mountDeleteById ==== end ====");
        return JSON.toJSONString(id);
    }

    @RequestMapping(method = RequestMethod.GET, value = RequestMappingConstants.DELETE)
    @ResponseBody
    public String deleteMountTable() {
        mountDao.deleteAll();
        return "";
    }
}
