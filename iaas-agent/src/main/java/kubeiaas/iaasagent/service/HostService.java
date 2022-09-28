package kubeiaas.iaasagent.service;

import com.alibaba.fastjson.JSONArray;
import kubeiaas.common.bean.Host;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.HostConstants;
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

    public void checkEnv(String type) {
        switch (type) {
            case HostConstants.CHECKER_DIR:
                ShellUtils.getCmd(String.format(HostConfig.CMD_RUN_CHECKER, HostConstants.CHECKER_DIR, ""));
                break;

            case HostConstants.CHECKER_KVM:
                ShellUtils.getCmd(String.format(HostConfig.CMD_RUN_CHECKER, HostConstants.CHECKER_KVM, ""));
                break;

            case HostConstants.CHECKER_LIBVIRT:
                ShellUtils.getCmd(String.format(HostConfig.CMD_RUN_CHECKER, HostConstants.CHECKER_LIBVIRT, ""));
                break;

            case HostConstants.CHECKER_MNT:
            case HostConstants.CHECKER_MNT_EXPORT:
                if (hasHostRole(HostConstants.ROLE_MNT)) {
                    // mount root
                    ShellUtils.getCmd(String.format(HostConfig.CMD_RUN_CHECKER, HostConstants.CHECKER_MNT_EXPORT, "-m " + hostConfig.getNetwork_subnet_with_mask()));
                } else {
                    // not mnt
                    Host host = getHostByRole(HostConstants.ROLE_MNT);
                    String mntIp = (host == null) ? "" : host.getIp();
                    ShellUtils.getCmd(String.format(HostConfig.CMD_RUN_CHECKER, HostConstants.CHECKER_MNT, "-m " + mntIp));
                }
                break;

            case HostConstants.CHECKER_DHCP:
                if (hasHostRole(HostConstants.ROLE_DHCP)) {
                    ShellUtils.getCmd(String.format(HostConfig.CMD_RUN_CHECKER, HostConstants.CHECKER_DHCP, "-b " +
                            LibvirtConfig.privateNetwork + " -s " + hostConfig.getNetwork_subnet() + " -m " +
                            hostConfig.getNetwork_netmask() + " -g " + hostConfig.getNetwork_gateway()));
                } else {
                    log.info("this node not support dhcp");
                }
                break;

            case HostConstants.CHECKER_VNC:
                if (hasHostRole(HostConstants.ROLE_VNC)) {
                    ShellUtils.getCmd(String.format(HostConfig.CMD_RUN_CHECKER, HostConstants.CHECKER_VNC));
                } else {
                    log.info("this node not support vnc");
                }
                break;

            default:
                log.error("Unknown check type!");
                break;
        }
    }

    public boolean checkEnvRes(String type) {
        try {
            int res;
            res = getEnvCheckRes(type);
            while (res == -1) {
                res = getEnvCheckRes(type);
                TimeUnit.SECONDS.sleep(5);
                log.info("...wait for checking, res code " + res);
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

}
