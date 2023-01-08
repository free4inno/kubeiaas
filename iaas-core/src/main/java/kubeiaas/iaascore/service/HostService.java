package kubeiaas.iaascore.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.bean.HostConstants;
import kubeiaas.common.enums.host.HostStatusEnum;
import kubeiaas.iaascore.config.ServiceConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.scheduler.HostScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service
public class HostService {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private HostScheduler hostScheduler;

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
     * 获取统计表
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> resMap = new HashMap<>();
        List<Host> hostList = tableStorage.hostQueryAll();

        // 1. total
        resMap.put(HostConstants.TOTAL, hostList.size());

        // 2. status
        resMap.put(HostConstants.ACTIVE, (int) hostList.stream()
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
        Integer totalDataStorage = 0;
        Integer usedDataStorage = 0;

        // calculate used
        List<Vm> vmList = tableStorage.vmQueryAll();
        for (Vm vm : vmList) {
            usedVCPU += vm.getCpus();
            usedMEM += vm.getMemory();
            usedSysStorage += vm.getDiskSize();
        }

        List<Volume> dataVolumeList = tableStorage.volumeQueryAllDataVolume();
        for (Volume volume : dataVolumeList) {
            usedDataStorage += volume.getSize();
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
        resMap.put(HostConstants.TOTAL_STORAGE, totalSysStorage);
        resMap.put(HostConstants.USED_STORAGE, usedSysStorage);

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
