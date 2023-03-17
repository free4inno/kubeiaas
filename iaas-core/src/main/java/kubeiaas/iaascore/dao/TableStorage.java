package kubeiaas.iaascore.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import kubeiaas.common.bean.*;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.*;
import kubeiaas.common.enums.config.SpecTypeEnum;
import kubeiaas.common.enums.volume.VolumeUsageEnum;
import kubeiaas.iaascore.dao.feign.DbProxy;
import kubeiaas.iaascore.dao.feign.ImageOperator;
import kubeiaas.iaascore.request.image.SaveImageForm;
import kubeiaas.iaascore.response.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

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

    public PageResponse<Vm> vmFuzzyQuery(String keywords, String status, String hostUuid, String imageUuid, Integer pageNum, Integer pageSize) {
        String jsonString = dbProxy.vmFuzzyQuery(keywords, status, hostUuid, imageUuid, pageNum, pageSize);
        return JSON.parseObject(jsonString, new TypeReference<PageResponse<Vm>>(){});
    }

    public PageResponse<Vm> vmFuzzyQueryAttach(String keywords, Integer pageNum, Integer pageSize) {
        String jsonString = dbProxy.vmFuzzyQueryAttach(keywords, pageNum, pageSize);
        return JSON.parseObject(jsonString, new TypeReference<PageResponse<Vm>>(){});
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

    public PageResponse<Vm> vmPageQueryAll(Integer pageNum, Integer pageSize) {
        String jsonString = dbProxy.vmPageQueryAll(pageNum, pageSize);
        return JSON.parseObject(jsonString, new TypeReference<PageResponse<Vm>>(){});
    }

    public Vm vmSave(Vm vm) {
        String vmObjectStr = JSON.toJSONString(vm);
        vmObjectStr = dbProxy.vmSave(vmObjectStr);
        return JSON.parseObject(vmObjectStr, Vm.class);
    }

    public void vmDeleteByUuid(String vmUuid){
        dbProxy.vmDeleteByUuid(vmUuid);
    }

    public Vm vmUpdate(Vm vm){
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

    public PageResponse<Image> imagePageQueryAll(Integer pageNum, Integer pageSize) {
        String jsonString = imageOperator.imagePageQueryAll(pageNum, pageSize);
        return JSON.parseObject(jsonString, new TypeReference<PageResponse<Image>>(){});
    }

    public Image imageQueryByUuid(String uuid) {
        String jsonString = imageOperator.imageQueryByUuid(uuid);
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        } else {
            return JSON.parseObject(jsonString, Image.class);
        }
    }

    public PageResponse<Image> imageFuzzyQuery(String keywords, Integer pageNum, Integer pageSize) {
        String jsonString = imageOperator.imageFuzzyQuery(keywords, pageNum, pageSize);
        return JSON.parseObject(jsonString, new TypeReference<PageResponse<Image>>(){});
    }

    public String imageGetRaw(String uuid) {
        return imageOperator.imageQueryRawByUuid(uuid);
    }

    public boolean imageSave(String uuid, String content) {
        SaveImageForm f = new SaveImageForm(uuid, content);
        return imageOperator.imageSaveYaml(f).equals(ResponseMsgConstants.SUCCESS);
    }

    public boolean imageDelete(String uuid) {
        return imageOperator.imageDelete(uuid).equals(ResponseMsgConstants.SUCCESS);
    }

    public Integer imageTotalNum() {
        return imageOperator.statistics();
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

    public Host hostQueryByName(String name) {
        String jsonString = dbProxy.hostQueryAllBySingleKey(HostConstants.NAME, name);
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

    public Host hostSave(Host host) {
        String hostObjectStr = JSON.toJSONString(host);
        hostObjectStr = dbProxy.hostSave(hostObjectStr);
        return JSON.parseObject(hostObjectStr, Host.class);
    }

    // ========================= ip segment =========================

    public List<IpSegment> ipSegmentQueryAllByHostAndType(String hostUuid, String type) {
        String jsonString = dbProxy.ipSegmentQueryAllByDoubleKey(
                IpSegmentConstants.HOST_UUID, hostUuid, IpSegmentConstants.TYPE, type);
        return JSON.parseArray(jsonString, IpSegment.class);
    }

    public IpSegment ipSegmentQueryById(int id) {
        String jsonString = dbProxy.ipSegmentQueryAllBySingleKey(IpSegmentConstants.ID, Integer.toString(id));
        List<IpSegment> ipSegmentList = JSON.parseArray(jsonString, IpSegment.class);
        if (ipSegmentList != null && !ipSegmentList.isEmpty()) {
            return ipSegmentList.get(0);
        } else {
            return null;
        }
    }

    public List<IpSegment> ipSegmentQueryAll() {
        String jsonString = dbProxy.ipSegmentQueryAll();
        return JSON.parseArray(jsonString, IpSegment.class);
    }

    public IpSegment ipSegmentSave(IpSegment ipSegment) {
        String ipSegmentObjectStr = JSON.toJSONString(ipSegment);
        ipSegmentObjectStr = dbProxy.ipSegmentSave(ipSegmentObjectStr);
        return JSON.parseObject(ipSegmentObjectStr, IpSegment.class);
    }

    public void ipSegmentDelete(Integer ipSegmentId){
        dbProxy.ipSegmentDeleteById(ipSegmentId);
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

    public void ipUsedDeleteByVmUuid(String vmUuid){
        dbProxy.ipUsedDeleteByVmUuid(vmUuid);
    }

    // ========================= volume =========================

    public List<Volume> volumeQueryAllByInstanceUuid(String instanceUuid) {
        String jsonString = dbProxy.volumeQueryAllBySingleKey(VolumeConstants.INSTANCE_UUID, instanceUuid);
        return JSON.parseArray(jsonString, Volume.class);
    }

    public List<Volume> volumeQueryAllDataVolume() {
        String jsonString = dbProxy.volumeQueryAllDataVolume();
        return JSON.parseArray(jsonString, Volume.class);
    }

    public PageResponse<Volume> volumeFuzzyQueryDataVolume(String keywords, String status ,Integer pageNum, Integer pageSize) {
        String jsonString = dbProxy.volumeFuzzyQueryDataVolume(keywords, status, pageNum, pageSize);
        return JSON.parseObject(jsonString, new TypeReference<PageResponse<Volume>>(){});
    }

    public PageResponse<Volume> volumePageQueryAll(Integer pageNum, Integer pageSize) {
        String jsonString = dbProxy.volumePageQueryAllDataVolume(pageNum, pageSize);
        return JSON.parseObject(jsonString, new TypeReference<PageResponse<Volume>>(){});
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

    public Volume systemVolumeQueryByUuid(String uuid) {
        String jsonString = dbProxy.volumeQueryAllBySingleKey(VolumeConstants.INSTANCE_UUID, uuid);
        List<Volume> volumeList = JSON.parseArray(jsonString, Volume.class);
        if (volumeList != null && !volumeList.isEmpty()) {
            volumeList = volumeList.stream().filter(volume1 -> volume1.getUsageType().equals(VolumeUsageEnum.SYSTEM)).collect(Collectors.toList());
            return volumeList.get(0);
        }else {
            return null;
        }
    }

    public Volume volumeSave(Volume volume) {
        String volumeObjectStr = JSON.toJSONString(volume);
        volumeObjectStr = dbProxy.volumeSave(volumeObjectStr);
        return JSON.parseObject(volumeObjectStr, Volume.class);
    }

    public void volumeDelete(String volumeUuid){
        dbProxy.volumeDeleteByUuid(volumeUuid);
    }

    public Volume volumeUpdate(Volume volume){
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
