package kubeiaas.iaasagent.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import kubeiaas.common.bean.Host;
import kubeiaas.common.constants.bean.HostConstants;
import kubeiaas.common.enums.host.HostStatusEnum;
import kubeiaas.common.utils.ShellUtils;
import kubeiaas.common.utils.UuidUtils;
import kubeiaas.iaasagent.dao.TableStorage;
import kubeiaas.iaasagent.service.HostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Configuration
public class HostConfig {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private HostService hostService;

    private String hostIp = "";
    private String hostName = "";

    private final String network_subnet = System.getenv("NETWORK_SUBNET");
    private final String network_netmask = System.getenv("NETWORK_NETMASK");
    private final String network_subnet_with_mask = System.getenv("NETWORK_SUBNET_WITH_MASK");
    private final String network_gateway = System.getenv("NETWORK_GATEWAY");

    public static String CMD_CPU_CORE = "cat /proc/cpuinfo| grep \"processor\" | wc -l";
    public static String CMD_CPU_MHZ = "cat /proc/cpuinfo | grep MHz|head -1|awk '{print $4}'";
    public static String CMD_MEM_GB = "cat /proc/meminfo | grep MemTotal | awk '{print $2/1024/1024}'";
    public static String CMD_DISK_GB = "df | grep '/$' | awk '{print int($2*1/1024/1024)}'";
    public static String CMD_VERSION = "cat /etc/redhat-release";

    public static String CMD_RUN_CHECKER = "sh -c /usr/local/kubeiaas/workdir/checker/%s-checker.sh %s >> /usr/local/kubeiaas/workdir/log/iaas-agent.log";
    public static String RESULT_CHECKER = "/usr/local/kubeiaas/workdir/log/checkResult-%s.log";

    public void hostRegister() {
        // get localhost hostIp & hostName
        hostIp = System.getenv("HOST_IP");
        hostName = System.getenv("HOST_NAME");
        log.info("hostIp:" + hostIp + " hostName:" + hostName);

        // check is this host registered in DB
        Host host = tableStorage.hostQueryByIp(hostIp);
        if (host == null) {
            // do register
            log.info("this host is not registered! Start to register.");
            host = new Host();
            host.setIp(hostIp);
            host.setName(hostName);

            // - set Role (empty or mnt)
            JSONArray roleArray = new JSONArray();
            List<Host> hostList = tableStorage.hostQueryAll();
            if (hostList.isEmpty()) {
                log.info("this host is the first to register, set `mnt`");
                roleArray.add(HostConstants.ROLE_MNT);
            }
            host.setRole(roleArray.toJSONString());

            // - set status
            host.setStatus(HostStatusEnum.PREPARING);

            // - set config
            String cpuCore = ShellUtils.getCmd(CMD_CPU_CORE);
            String cpuMhz = ShellUtils.getCmd(CMD_CPU_MHZ);
            String diskSize = ShellUtils.getCmd(CMD_DISK_GB);
            String memSize = ShellUtils.getCmd(CMD_MEM_GB);
            String os = ShellUtils.getCmd(CMD_VERSION);
            host.setConfig(String.format("系统版本：%s；处理器：%s核心，%sMHz；内存：%sGB；磁盘：%sGB", os, cpuCore, cpuMhz, memSize, diskSize));

            // - generate uuid
            host.setUuid(UuidUtils.getRandomUuid());

            // save into DB
            host = tableStorage.hostSave(host);
            log.info(JSON.toJSONString(host));

            // check and install in the first time
            boolean totalSuccessFlag;
            totalSuccessFlag = checkHostEnv(HostConstants.CHECKER_DIR);
            totalSuccessFlag = checkHostEnv(HostConstants.CHECKER_KVM) && totalSuccessFlag;
            totalSuccessFlag = checkHostEnv(HostConstants.CHECKER_MNT) && totalSuccessFlag;
            totalSuccessFlag = checkHostEnv(HostConstants.CHECKER_LIBVIRT) && totalSuccessFlag;
            if (totalSuccessFlag) {
                host.setStatus(HostStatusEnum.READY);
                log.info("host check done, total success.");
            } else {
                host.setStatus(HostStatusEnum.ERROR);
                log.error("host check done, total failed!");
            }
            tableStorage.hostSave(host);

        } else {
            log.info("this host is registered.");
            // check only
            boolean totalSuccessFlag;
            totalSuccessFlag = checkHostEnv(HostConstants.CHECKER_DIR);
            totalSuccessFlag = checkHostEnv(HostConstants.CHECKER_KVM) && totalSuccessFlag;
            totalSuccessFlag = checkHostEnv(HostConstants.CHECKER_MNT) && totalSuccessFlag;
            totalSuccessFlag = checkHostEnv(HostConstants.CHECKER_LIBVIRT) && totalSuccessFlag;
            totalSuccessFlag = checkHostEnv(HostConstants.CHECKER_DHCP) && totalSuccessFlag;
            totalSuccessFlag = checkHostEnv(HostConstants.CHECKER_VNC) && totalSuccessFlag;
            if (totalSuccessFlag) {
                host.setStatus(HostStatusEnum.READY);
                log.info("host check done, total success.");
            } else {
                host.setStatus(HostStatusEnum.ERROR);
                log.error("host check done, total failed!");
            }
            tableStorage.hostSave(host);
        }
    }

    private boolean checkHostEnv(String type) {
        hostService.checkEnv(type);
        if (hostService.checkEnvRes(type)) {
            log.info("...host checking, [{}] success.", type);
            return true;
        } else {
            log.warn("...host checking, [{}] failed!", type);
            return false;
        }
    }

    public String getHostIp() {
        return hostIp;
    }

    public String getHostName() {
        return hostName;
    }

    public String getNetwork_subnet() {
        return network_subnet;
    }

    public String getNetwork_netmask() {
        return network_netmask;
    }

    public String getNetwork_subnet_with_mask() {
        return network_subnet_with_mask;
    }

    public String getNetwork_gateway() {
        return network_gateway;
    }
}
