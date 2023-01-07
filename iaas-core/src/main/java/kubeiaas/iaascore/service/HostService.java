package kubeiaas.iaascore.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import kubeiaas.common.bean.Host;
import kubeiaas.common.constants.bean.HostConstants;
import kubeiaas.common.enums.host.HostStatusEnum;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.scheduler.HostScheduler;
import lombok.extern.slf4j.Slf4j;
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

        return resMap;
    }
}
