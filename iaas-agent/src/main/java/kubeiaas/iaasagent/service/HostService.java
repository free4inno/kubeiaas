package kubeiaas.iaasagent.service;

import com.alibaba.fastjson2.JSONArray;
import kubeiaas.common.bean.Host;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.HostConstants;
import kubeiaas.common.enums.host.HostStatusEnum;
import kubeiaas.common.utils.FileUtils;
import kubeiaas.iaasagent.config.HostConfig;
import kubeiaas.iaasagent.dao.TableStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class HostService {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private HostConfig hostConfig;

    /**
     * 获取环境配置结果
     * @return boolean result (true: 成功；false：失败）
     */
    public boolean getEnvPrepareRes(List<String> roleList) {
        log.info(String.format("getEnvPrepareRes info -- roleList: %s", roleList));
        int MAX_CNT = 12;

        List<String> typeList = new ArrayList<>();
        typeList.add(HostConstants.CHECKER_DIR);
        typeList.add(HostConstants.CHECKER_JAVA);
        typeList.add(HostConstants.CHECKER_KVM);
        typeList.add(HostConstants.CHECKER_LIBVIRT);
        if (roleList.contains(HostConstants.ROLE_DHCP)) {
            typeList.add(HostConstants.CHECKER_DHCP);
        }
        if (!roleList.contains(HostConstants.ROLE_NFS)) {
            typeList.add(HostConstants.CHECKER_MNT);
        }

        for (String type : typeList) {
            try {
                int cnt = 0;
                int res;
                res = getEnvPreResByType(type);
                while (res == -1 && cnt < MAX_CNT) {
                    cnt += 1;
                    TimeUnit.SECONDS.sleep(5);
                    log.debug("......wait for checking [{}], res code {}", type, res);
                    res = getEnvPreResByType(type);
                }
                if (res == 0) {
                    return false;
                }
            } catch (InterruptedException e) {
                log.warn("getEnvPrepareRes -- InterruptedException: {}", e.getMessage());
            }
        }
        return true;
    }

    private int getEnvPreResByType(String type) {
        log.info(String.format("getEnvPreResByType info -- type: %s", type));
        String res = FileUtils.readFile(HostConfig.RESULT_PREPARE).getProperty(type);
        if (res == null) {
            return -1;
        } else if (res.equals(ResponseMsgConstants.SUCCESS)) {
            return 1;
        } else if (res.equals(ResponseMsgConstants.FAILED)) {
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * 判断当前节点 host 是否具有该角色 role
     */
    public boolean hasHostRole(String roleName) {
        log.info(String.format("hasHostRole info -- roleName: %s", roleName));

        log.info("-> invoke DB -- hostQueryByIp");
        Host host = tableStorage.hostQueryByIp(hostConfig.getHostIp());
        log.info("<- invoke DB -- done");
        JSONArray roles = JSONArray.parseArray(host.getRole());
        return roles.contains(roleName);
    }

    /**
     * 为当前节点 host 加入角色 role
     */
    public void setHostRole(String roleName) {
        log.info(String.format("setHostRole info -- roleName: %s", roleName));

        log.info("-> invoke DB -- hostQueryByIp");
        Host host = tableStorage.hostQueryByIp(hostConfig.getHostIp());
        log.info("<- invoke DB -- done");
        JSONArray roles = JSONArray.parseArray(host.getRole());
        roles.add(roleName);
        host.setRole(roles.toJSONString());
        tableStorage.hostSave(host);
    }

    /**
     * 为当前节点 host 删除角色 role
     */
    public void delHostRole(String roleName) {
        log.info(String.format("delHostRole info -- roleName: %s", roleName));

        log.info("-> invoke DB -- hostQueryByIp");
        Host host = tableStorage.hostQueryByIp(hostConfig.getHostIp());
        log.info("<- invoke DB -- done");
        JSONArray roles = JSONArray.parseArray(host.getRole());
        roles.remove(roleName);
        host.setRole(roles.toJSONString());
        tableStorage.hostSave(host);
    }

    /**
     * 获取对应角色的节点 host
     */
    public Host getHostByRole(String roleName) {
        log.info(String.format("getHostByRole info -- roleName: %s", roleName));
        log.info("-> invoke DB -- hostQueryByRole");
        Host host = tableStorage.hostQueryByRole(roleName);
        log.info("<- invoke DB -- done");
        return host;
    }

    /**
     * 设置当前节点 host 状态
     */
    public void setHostStatus(HostStatusEnum hostStatus) {
        log.info(String.format("setHostStatus info -- hostStatus: %s", hostStatus));

        log.info("-> invoke DB -- hostQueryByIp");
        Host host = tableStorage.hostQueryByIp(hostConfig.getHostIp());
        log.info("<- invoke DB -- done");
        if (!host.getStatus().equals(hostStatus)) {
            host.setStatus(hostStatus);
        }
        log.info("-> invoke DB -- hostSave");
        tableStorage.hostSave(host);
        log.info("<- invoke DB -- done");
    }
}
