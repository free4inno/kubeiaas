package kubeiaas.iaasagent.dao;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.*;
import kubeiaas.common.constants.bean.*;
import kubeiaas.common.enums.config.SpecTypeEnum;
import kubeiaas.iaasagent.dao.feign.DbProxy;
import kubeiaas.iaasagent.dao.feign.ImageOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class TableStorage {

    @Resource
    private DbProxy dbProxy;

    @Resource
    private ImageOperator imageOperator;

    // ========================= vm =========================

    public List<Vm> vmQueryAll() {
        String jsonString = dbProxy.vmQueryAll();
        return JSON.parseArray(jsonString, Vm.class);
    }

    public Vm vmQueryByUuid(String uuid) {
        String jsonString = dbProxy.vmQueryAllBySingleKey(VmConstants.UUID, uuid);
        List<Vm> vmList = JSON.parseArray(jsonString, Vm.class);
        if (vmList != null && !vmList.isEmpty()) {
            return vmList.get(0);
        } else {
            return null;
        }
    }

    public Vm vmSave(Vm vm) {
        String vmObjectStr = JSON.toJSONString(vm);
        vmObjectStr = dbProxy.vmSave(vmObjectStr);
        return JSON.parseObject(vmObjectStr, Vm.class);
    }

    // ========================= image =========================

    /**
     * !!! ATTENTION !!!
     *
     * - 关于 image 的管理起初是在数据库内进行存储，feign interface 位于 dbProxy，
     *   因此通过本 tableStorage 进行聚合处理。
     *
     * - 引入 imageOperator 镜像管理模块 后，feign interface 位于 resourceOperator，
     *   但为了屏蔽对各处上层代码调用的影响，且在逻辑上保持单纯的 镜像信息持久化 概念，特在 tableStorage 调用了该 operator。
     */

    public List<Image> imageQueryAll() {
        String jsonString = imageOperator.imageQueryAll();
        return JSON.parseArray(jsonString, Image.class);
    }

    public Image imageQueryByUuid(String uuid) {
        String jsonString = imageOperator.imageQueryByUuid(uuid);
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        } else {
            return JSON.parseObject(jsonString, Image.class);
        }
    }

    /*
    public Image imageQueryByUuid(String uuid) {
        String jsonString = dbProxy.imageQueryAllBySingleKey(ImageConstants.UUID, uuid);
        List<Image> imageList = JSON.parseArray(jsonString, Image.class);
        if (imageList != null && !imageList.isEmpty()) {
            return imageList.get(0);
        } else {
            return null;
        }
    }

    public List<Image> imageQueryAll() {
        String jsonString = dbProxy.imageQueryAll();
        return JSON.parseArray(jsonString, Image.class);
    }
     */

    // ========================= host =========================

    public List<Host> hostQueryAll() {
        String jsonString = dbProxy.hostQueryAll();
        return JSON.parseArray(jsonString, Host.class);
    }

    public Host hostQueryByUuid(String uuid) {
        String jsonString = dbProxy.hostQueryAllBySingleKey(HostConstants.UUID, uuid);
        List<Host> hostList = JSON.parseArray(jsonString, Host.class);
        if (hostList != null && !hostList.isEmpty()) {
            return hostList.get(0);
        } else {
            return null;
        }
    }

    public Host hostQueryByRole(String role) {
        String jsonString = dbProxy.hostQueryAllLikeBySingleKey(HostConstants.ROLE, "\"" + role + "\"");
        List<Host> hostList = JSON.parseArray(jsonString, Host.class);
        if (hostList != null && !hostList.isEmpty()) {
            return hostList.get(0);
        } else {
            return null;
        }
    }

    public Host hostQueryByIp(String ip) {
        String jsonString = dbProxy.hostQueryAllBySingleKey(HostConstants.IP, ip);
        List<Host> hostList = JSON.parseArray(jsonString, Host.class);
        if (hostList != null && !hostList.isEmpty()) {
            return hostList.get(0);
        } else {
            return null;
        }
    }

    public Host hostSave(Host host) {
        String hostObjectStr = JSON.toJSONString(host);
        hostObjectStr = dbProxy.hostSave(hostObjectStr);
        return JSON.parseObject(hostObjectStr, Host.class);
    }

    // ========================= ip segment =========================

    public IpSegment ipSegmentQueryById(int id) {
        String jsonString = dbProxy.ipSegmentQueryAllBySingleKey(IpSegmentConstants.ID, Integer.toString(id));
        List<IpSegment> ipSegmentList = JSON.parseArray(jsonString, IpSegment.class);
        if (ipSegmentList != null && !ipSegmentList.isEmpty()) {
            return ipSegmentList.get(0);
        } else {
            return null;
        }
    }

    // ========================= ip used =========================

    public List<IpUsed> ipUsedQueryAllByInstanceUuid(String instanceUuid) {
        String jsonString = dbProxy.ipUsedQueryAllBySingleKey(IpUsedConstants.INSTANCE_UUID, instanceUuid);
        return JSON.parseArray(jsonString, IpUsed.class);
    }

    public List<IpUsed> ipUsedQueryAllByIpSegmentId(int ipSegmentId) {
        String jsonString = dbProxy.ipUsedQueryAllBySingleKey(IpUsedConstants.IP_SEGMENT_ID, Integer.toString(ipSegmentId));
        return JSON.parseArray(jsonString, IpUsed.class);
    }

    public IpUsed ipUsedQueryById(int id) {
        String jsonString = dbProxy.ipUsedQueryAllBySingleKey(IpSegmentConstants.ID, Integer.toString(id));
        List<IpUsed> ipUsedList = JSON.parseArray(jsonString, IpUsed.class);
        if (ipUsedList != null && !ipUsedList.isEmpty()) {
            return ipUsedList.get(0);
        } else {
            return null;
        }
    }

    public IpUsed ipUsedQueryByIp(String ip) {
        String jsonString = dbProxy.ipUsedQueryAllBySingleKey(IpSegmentConstants.IP, ip);
        List<IpUsed> ipUsedList = JSON.parseArray(jsonString, IpUsed.class);
        if (ipUsedList != null && !ipUsedList.isEmpty()) {
            return ipUsedList.get(0);
        } else {
            return null;
        }
    }

    public IpUsed ipUsedSave(IpUsed ipUsed) {
        String ipUsedObjectStr = JSON.toJSONString(ipUsed);
        ipUsedObjectStr = dbProxy.ipUsedSave(ipUsedObjectStr);
        return JSON.parseObject(ipUsedObjectStr, IpUsed.class);
    }

    // ========================= volume =========================

    public List<Volume> volumeQueryAllByInstanceUuid(String instanceUuid) {
        // TODO: api-common package may error
//        String jsonString = dbProxy.volumeQueryAllBySingleKey(VolumeConstants.INSTANCE_UUID, instanceUuid);
        String jsonString = dbProxy.volumeQueryAllBySingleKey("instanceUuid", instanceUuid);
        return JSON.parseArray(jsonString, Volume.class);
    }

    public Volume volumeQueryByUuid(String uuid) {
        String jsonString = dbProxy.volumeQueryAllBySingleKey(VolumeConstants.UUID, uuid);
        List<Volume> volumeList = JSON.parseArray(jsonString, Volume.class);
        if (volumeList != null && !volumeList.isEmpty()) {
            return volumeList.get(0);
        } else {
            return null;
        }
    }

    public Volume volumeSave(Volume volume) {
        String volumeObjectStr = JSON.toJSONString(volume);
        volumeObjectStr = dbProxy.volumeSave(volumeObjectStr);
        return JSON.parseObject(volumeObjectStr, Volume.class);
    }

    // ========================= specConfig =========================

    public List<SpecConfig> specConfigQueryAllByType(SpecTypeEnum type) {
        String jsonString = dbProxy.specConfigQueryAllByType(type);
        return JSON.parseArray(jsonString, SpecConfig.class);
    }
}
