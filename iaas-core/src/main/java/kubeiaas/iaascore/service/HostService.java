package kubeiaas.iaascore.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.bean.HostConstants;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.host.HostStatusEnum;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.config.ServiceConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.scheduler.HostScheduler;
import kubeiaas.iaascore.scheduler.VolumeScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HostService {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private HostScheduler hostScheduler;

    @Resource
    private VolumeScheduler volumeScheduler;

    @Resource
    private ServiceConfig serviceConfig;

    /**
     * 设置节点角色
     */
    public Host setRole(String hostUuid, String hostRole) throws BaseException {
        // 1. get host from db
        Host host = tableStorage.hostQueryByUuid(hostUuid);
        if (host == null) {
            throw new BaseException("ERROR: host not found!");
        }
        JSONArray roles = JSON.parseArray(host.getRole());
        if (roles.contains(hostRole)) {
            throw new BaseException("ERROR: role is already set!");
        }

        // 2. check and set role
        String checkerName;
        switch (hostRole) {
            case HostConstants.ROLE_DHCP:
                roles.add(HostConstants.ROLE_DHCP);
                checkerName = HostConstants.CHECKER_DHCP;
                break;
            case HostConstants.ROLE_VNC:
                roles.add(HostConstants.ROLE_VNC);
                checkerName = HostConstants.ROLE_VNC;
                break;
            default:
                throw new BaseException("ERROR: role is illegal!");
        }

        // 3. save into DB
        host.setRole(roles.toJSONString());
        host = tableStorage.hostSave(host);

        // 4. agent do config
        hostScheduler.configEnv(host.getIp(), checkerName);

        return host;
    }

    /**
     * 获取统计数据
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> resMap = new HashMap<>();
        List<Host> hostList = tableStorage.hostQueryAll();

        // 1. total
        resMap.put(HostConstants.TOTAL_NODE, hostList.size());

        // 2. status
        resMap.put(HostConstants.ACTIVE_NODE, (int) hostList.stream()
                .filter((Host h) -> h.getStatus().equals(HostStatusEnum.READY)).count());

        // 3. resource

        // -- 3.1. vCPU --------------
        Integer totalVCPU = 0;
        Integer usedVCPU = 0;
        // -- 3.2. MEM ---------------
        Integer totalMEM = 0;
        Integer usedMEM = 0;
        // -- 3.3. STORAGE -----------
        Integer totalSysStorage = 0;
        Integer usedSysStorage = 0;
        int totalDataStorage = 0;
        int usedDataStorage = 0;
        int totalImgStorage = 0;
        int usedImgStorage = 0;

        // calculate used
        List<Vm> vmList = tableStorage.vmQueryAll();
        for (Vm vm : vmList) {
            usedVCPU += vm.getCpus();
            usedMEM += vm.getMemory();
            usedSysStorage += vm.getDiskSize();
        }

        // calculate total
        for (Host host : hostList) {
            totalVCPU += host.getVCPU();
            totalMEM += host.getMemory();
            totalSysStorage += host.getStorage();
        }

        resMap.put(HostConstants.TOTAL_vCPU, totalVCPU);
        resMap.put(HostConstants.USED_vCPU, usedVCPU);
        resMap.put(HostConstants.TOTAL_MEM, totalMEM);
        resMap.put(HostConstants.USED_MEM, usedMEM);
        resMap.put(HostConstants.TOTAL_SYS_STORAGE, totalSysStorage);
        resMap.put(HostConstants.USED_SYS_STORAGE, usedSysStorage);

        // get nfs info
        List<Host> readyHosts = hostList.stream()
                .filter(h -> (h.getStatus().equals(HostStatusEnum.READY)))
                .collect(Collectors.toList());

        if (!readyHosts.isEmpty()) {
            Host node = hostList.get(0);

            AgentConfig.setSelectedHost(RequestMappingConstants.DATA_VOLUME_STORAGE, node);
            Map<String, String> dataVolStorageMap = volumeScheduler.getDataVolStorageInfo();
            totalDataStorage = Integer.parseInt(dataVolStorageMap.get(VolumeConstants.TOTAL));
            usedDataStorage = Integer.parseInt(dataVolStorageMap.get(VolumeConstants.USED));
            AgentConfig.clearSelectedHost(RequestMappingConstants.DATA_VOLUME_STORAGE);

            AgentConfig.setSelectedHost(RequestMappingConstants.IMG_VOLUME_STORAGE, node);
            Map<String, String> imgVolStorageMap = volumeScheduler.getImgVolStorageInfo();
            totalImgStorage = Integer.parseInt(imgVolStorageMap.get(VolumeConstants.TOTAL));
            usedImgStorage = Integer.parseInt(imgVolStorageMap.get(VolumeConstants.USED));
            AgentConfig.clearSelectedHost(RequestMappingConstants.IMG_VOLUME_STORAGE);
        }

        resMap.put(HostConstants.TOTAL_DATA_STORAGE, totalDataStorage);
        resMap.put(HostConstants.USED_DATA_STORAGE, usedDataStorage);
        resMap.put(HostConstants.TOTAL_IMG_STORAGE, totalImgStorage);
        resMap.put(HostConstants.USED_IMG_STORAGE, usedImgStorage);

        return resMap;
    }

    /**
     * 获取节点资源详细信息
     */
    public Map<String, Object> getNodeResource() {
        Map<String, Object> resMap = new HashMap<>();
        List<Host> hostList = tableStorage.hostQueryAll();
        // 1. total num
        resMap.put(HostConstants.TOTAL_NODE, hostList.size());
        // 2. build node map
        List<Vm> vmList = tableStorage.vmQueryAll();
        for (Host host : hostList) {
            Map<String, Object> nodeMap = new HashMap<>();

            nodeMap.put(HostConstants.TOTAL_vCPU, host.getVCPU());
            nodeMap.put(HostConstants.TOTAL_MEM, host.getMemory());
            nodeMap.put(HostConstants.TOTAL_SYS_STORAGE, host.getStorage());

            int usedVCPU = 0;
            int usedMEM = 0;
            int usedSTO = 0;
            for (Vm vm : vmList) {
                if (vm.getHostUuid().equals(host.getUuid())) {
                    usedVCPU += vm.getCpus();
                    usedMEM += vm.getMemory();
                    usedSTO += vm.getDiskSize();
                }
            }
            nodeMap.put(HostConstants.USED_vCPU, usedVCPU);
            nodeMap.put(HostConstants.USED_MEM, usedMEM);
            nodeMap.put(HostConstants.USED_SYS_STORAGE, usedSTO);

            nodeMap.put(HostConstants.STO_DIR, VolumeConstants.DEFAULT_NFS_SRV_PATH + VolumeConstants.VOLUME_PATH);
            resMap.put(host.getName(), nodeMap);
        }
        return resMap;
    }

    /**
     * 获取网络存储资源详细信息
     */
    public Map<String, Object> getNetStoResource() {
        Map<String, Object> resMap = new HashMap<>();
        List<Host> hostList = tableStorage.hostQueryAll();

        List<String> queryTypes = new ArrayList<>();
        queryTypes.add(RequestMappingConstants.DATA_VOLUME_STORAGE);
        queryTypes.add(RequestMappingConstants.IMG_VOLUME_STORAGE);

        // get nfs info
        boolean outerNfs = true;
        String nfsIp = "";
        for (Host host : hostList) {
            if (host.getRole().contains(HostConstants.ROLE_MNT)) {
                nfsIp = host.getIp();
                outerNfs = false;
            }
        }

        List<Host> readyHosts = hostList.stream()
                .filter(h -> (h.getStatus().equals(HostStatusEnum.READY)))
                .collect(Collectors.toList());

        for (String queryType : queryTypes) {
            int totalStorage = 0;
            int usedStorage = 0;
            String stoDir = "";

            if (!readyHosts.isEmpty()) {
                Host node = hostList.get(0);
                AgentConfig.setSelectedHost(queryType, node);

                Map<String, String> volStorageMap = new HashMap<>();
                switch (queryType) {
                    case RequestMappingConstants.DATA_VOLUME_STORAGE:
                        volStorageMap = volumeScheduler.getDataVolStorageInfo();
                        break;
                    case RequestMappingConstants.IMG_VOLUME_STORAGE:
                        volStorageMap = volumeScheduler.getImgVolStorageInfo();
                        break;
                }
                if (!volStorageMap.isEmpty()) {
                    totalStorage = Integer.parseInt(volStorageMap.get(VolumeConstants.TOTAL));
                    usedStorage = Integer.parseInt(volStorageMap.get(VolumeConstants.USED));
                    if (outerNfs) {
                        nfsIp = volStorageMap.get(VolumeConstants.MNT_FS).split(":")[0];
                    }
                    stoDir = volStorageMap.get(VolumeConstants.MNT_DIR);
                }

                AgentConfig.clearSelectedHost(queryType);
            }

            Map<String, Object> netStoMap = new HashMap<>();
            netStoMap.put(HostConstants.TOTAL_STORAGE, totalStorage);
            netStoMap.put(HostConstants.USED_STORAGE, usedStorage);
            netStoMap.put(HostConstants.NFS_IP, nfsIp);
            netStoMap.put(HostConstants.STO_DIR, stoDir);

            resMap.put(queryType, netStoMap);
        }

        return resMap;
    }

    /**
     * 定时任务
     * 根据 Agent 设置 host 状态
     */
    @Scheduled(cron = "0 0/1 * * * ?") // every 1 min
    private void cleanServiceList() {
        List<Host> hostList = tableStorage.hostQueryAll();
        for (Host host : hostList) {
            if (!serviceConfig.getAgent(host.getName())
                    && !host.getStatus().equals(HostStatusEnum.OFFLINE)) {
                // agent offline -> set host status offline
                host.setStatus(HostStatusEnum.OFFLINE);
                tableStorage.hostSave(host);
            }
        }
    }

}
