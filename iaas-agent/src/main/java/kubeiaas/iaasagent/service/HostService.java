package kubeiaas.iaasagent.service;

import com.alibaba.fastjson.JSONArray;
import kubeiaas.common.bean.Host;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.HostConstants;
import kubeiaas.common.enums.host.HostStatusEnum;
import kubeiaas.common.utils.FileUtils;
import kubeiaas.common.utils.ShellUtils;
import kubeiaas.iaasagent.config.HostConfig;
import kubeiaas.iaasagent.config.LibvirtConfig;
import kubeiaas.iaasagent.dao.TableStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class HostService {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private HostConfig hostConfig;

    public boolean checkEnv(String type) {
        switch (type) {
            case HostConstants.CHECKER_DIR:
                ShellUtils.getCmd(String.format(HostConfig.CMD_REFRESH_RES, HostConstants.CHECKER_DIR));
                ShellUtils.run(String.format(HostConfig.CMD_RUN_CHECKER, HostConstants.CHECKER_DIR, ""));
                return true;

            case HostConstants.CHECKER_KVM:
                ShellUtils.getCmd(String.format(HostConfig.CMD_REFRESH_RES, HostConstants.CHECKER_KVM));
                ShellUtils.run(String.format(HostConfig.CMD_RUN_CHECKER, HostConstants.CHECKER_KVM, ""));
                return true;

            case HostConstants.CHECKER_LIBVIRT:
                ShellUtils.getCmd(String.format(HostConfig.CMD_REFRESH_RES, HostConstants.CHECKER_LIBVIRT));
                ShellUtils.run(String.format(HostConfig.CMD_RUN_CHECKER, HostConstants.CHECKER_LIBVIRT, ""));
                return true;

            case HostConstants.CHECKER_MNT:
                if (hasHostRole(HostConstants.ROLE_MNT)) {
                    // mount root
                    return false;
                } else {
                    // not mnt
                    Host host = getHostByRole(HostConstants.ROLE_MNT);
                    String mntIp = (host == null) ? "" : host.getIp();
                    ShellUtils.getCmd(String.format(HostConfig.CMD_REFRESH_RES, HostConstants.CHECKER_MNT));
                    String cmd = String.format(HostConfig.CMD_RUN_CHECKER, HostConstants.CHECKER_MNT, "-m " + mntIp);
                    log.info(cmd);
                    ShellUtils.run(cmd);
                    return true;
                }

            case HostConstants.CHECKER_MNT_EXPORT:
                if (hasHostRole(HostConstants.ROLE_MNT)) {
                    // mount root
                    ShellUtils.getCmd(String.format(HostConfig.CMD_REFRESH_RES, HostConstants.CHECKER_MNT_EXPORT));
                    ShellUtils.run(String.format(HostConfig.CMD_RUN_CHECKER, HostConstants.CHECKER_MNT_EXPORT, "-m " + hostConfig.getNetwork_subnet_with_mask()));
                    return true;
                } else {
                    // not mnt
                    return false;
                }

            case HostConstants.CHECKER_DHCP:
                if (hasHostRole(HostConstants.ROLE_DHCP)) {
                    ShellUtils.getCmd(String.format(HostConfig.CMD_REFRESH_RES, HostConstants.CHECKER_DHCP));
                    ShellUtils.run(String.format(HostConfig.CMD_RUN_CHECKER, HostConstants.CHECKER_DHCP, "-b " +
                            LibvirtConfig.privateNetwork + " -s " + hostConfig.getNetwork_subnet() + " -m " +
                            hostConfig.getNetwork_netmask() + " -g " + hostConfig.getNetwork_gateway()));
                    return true;
                } else {
                    log.info("`dhcp` is not in this host's roles.");
                    return false;
                }

            case HostConstants.CHECKER_VNC:
                if (hasHostRole(HostConstants.ROLE_VNC)) {
                    ShellUtils.getCmd(String.format(HostConfig.CMD_REFRESH_RES, HostConstants.CHECKER_VNC));
                    ShellUtils.run(String.format(HostConfig.CMD_RUN_CHECKER, HostConstants.CHECKER_VNC, ""));
                    return true;
                } else {
                    log.info("`vnc` is not in this host's roles.");
                    return false;
                }

            default:
                log.error("Unknown check type!");
                return false;
        }
    }

    public boolean checkEnvRes(String type) {
        try {
            int res;
            res = getEnvCheckRes(type);
            while (res == -1) {
                res = getEnvCheckRes(type);
                TimeUnit.SECONDS.sleep(5);
                log.info("......wait for checking [{}], res code {}", type, res);
            }
            return res == 1;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int getEnvCheckRes(String type) {
        String res = FileUtils.readFile(String.format(HostConfig.RESULT_CHECKER, type)).getProperty("result");
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

    public boolean hasHostRole(String roleName) {
        Host host = tableStorage.hostQueryByIp(hostConfig.getHostIp());
        JSONArray roles = JSONArray.parseArray(host.getRole());
        return roles.contains(roleName);
    }

    public Host getHostByRole(String roleName) {
        List<Host> hostList = tableStorage.hostQueryAll();
        for (Host host : hostList) {
            JSONArray roles = JSONArray.parseArray(host.getRole());
            if (roles.contains(roleName)) {
                return host;
            }
        }
        return null;
    }

    public Host setHostStatus(HostStatusEnum hostStatus) {
        Host host = tableStorage.hostQueryByIp(hostConfig.getHostIp());
        host.setStatus(hostStatus);
        return tableStorage.hostSave(host);
    }

}
