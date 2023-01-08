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
import kubeiaas.iaasagent.service.VncService;
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

    @Resource
    private VncService vncService;

    private final String hostIp = System.getenv("HOST_IP");
    private final String hostName = System.getenv("HOST_NAME");

    private final String network_subnet = System.getenv("NETWORK_SUBNET");
    private final String network_netmask = System.getenv("NETWORK_NETMASK");
    private final String network_subnet_with_mask = System.getenv("NETWORK_SUBNET_WITH_MASK");
    private final String network_gateway = System.getenv("NETWORK_GATEWAY");

    private final String vCPU = System.getenv(hostName.toUpperCase().replace("-", "_") + "_VCPU");
    private final String MEM = System.getenv(hostName.toUpperCase().replace("-", "_") + "_MEM");
    private final String STORAGE = System.getenv(hostName.toUpperCase().replace("-", "_") + "_STORAGE");

    public static String CMD_CPU_CORE = "cat /proc/cpuinfo| grep \"processor\" | wc -l";
    public static String CMD_CPU_MHZ = "cat /proc/cpuinfo | grep MHz|head -1|awk '{print $4}'";
    public static String CMD_MEM_GB = "cat /proc/meminfo | grep MemTotal | awk '{print $2/1024/1024}'";
    public static String CMD_DISK_GB = "df | grep '/$' | awk '{print int($2*1/1024/1024)}'";
    public static String CMD_VERSION = "cat /etc/redhat-release";

    public static String CMD_RUN_CHECKER = "sh /usr/local/kubeiaas/workdir/checker/%s-checker.sh %s >> /usr/local/kubeiaas/workdir/log/iaas-agent-checker.log";
    public static String CMD_REFRESH_RES = "echo \"result=unknown\" > /usr/local/kubeiaas/workdir/log/checkResult-%s.log";
    public static String CMD_REFRESH_LOG = "echo \"---\" > /usr/local/kubeiaas/workdir/log/iaas-agent-checker.log";
    public static String RESULT_CHECKER = "/usr/local/kubeiaas/workdir/log/checkResult-%s.log";

    public void hostInitialize() {
        // get host resource config
        log.info("hostIp:" + hostIp + ", hostName:" + hostName);
        log.info("vCPU:" + vCPU + ", MEM:" + MEM + ", STORAGE:" + STORAGE);

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

            // - set resource config
            host.setVCPU(Integer.parseInt(vCPU));
            host.setMemory(Integer.parseInt(MEM));
            host.setStorage(Integer.parseInt(STORAGE));

            // - generate uuid
            host.setUuid(UuidUtils.getRandomUuid());

            // save into DB
            host = tableStorage.hostSave(host);
            log.info(JSON.toJSONString(host));

            // check and install in the first time
            boolean totalSuccessFlag;
            ShellUtils.getCmd(CMD_REFRESH_LOG);
            totalSuccessFlag = checkHostEnvSync(HostConstants.CHECKER_DIR);
            totalSuccessFlag = checkHostEnvSync(HostConstants.CHECKER_KVM) && totalSuccessFlag;
//            totalSuccessFlag = checkHostEnvSync(HostConstants.CHECKER_MNT) && totalSuccessFlag;
//            totalSuccessFlag = checkHostEnvSync(HostConstants.CHECKER_MNT_EXPORT) && totalSuccessFlag;
            totalSuccessFlag = checkHostEnvSync(HostConstants.CHECKER_LIBVIRT) && totalSuccessFlag;
            if (totalSuccessFlag) {
                host.setStatus(HostStatusEnum.READY);
                log.info(" == host check done, total success. == ");
            } else {
                host.setStatus(HostStatusEnum.ERROR);
                log.error(" == host check done, total failed! == ");
            }
            tableStorage.hostSave(host);

            // check vnc
            vncService.checkVncStatus();

        } else {
            log.info("this host is registered.");
            // - set resource config
            host.setVCPU(Integer.parseInt(vCPU));
            host.setMemory(Integer.parseInt(MEM));
            host.setStorage(Integer.parseInt(STORAGE));

            // check only
            boolean totalSuccessFlag;
            ShellUtils.getCmd(CMD_REFRESH_LOG);
            totalSuccessFlag = checkHostEnvSync(HostConstants.CHECKER_DIR);
            totalSuccessFlag = checkHostEnvSync(HostConstants.CHECKER_KVM) && totalSuccessFlag;
//            totalSuccessFlag = checkHostEnvSync(HostConstants.CHECKER_MNT) && totalSuccessFlag;
//            totalSuccessFlag = checkHostEnvSync(HostConstants.CHECKER_MNT_EXPORT) && totalSuccessFlag;
            totalSuccessFlag = checkHostEnvSync(HostConstants.CHECKER_LIBVIRT) && totalSuccessFlag;
            totalSuccessFlag = checkHostEnvSync(HostConstants.CHECKER_DHCP) && totalSuccessFlag;
            // totalSuccessFlag = checkHostEnvSync(HostConstants.CHECKER_VNC) && totalSuccessFlag;
            // use container run vnc, only check is vnc running on this host.
            if (totalSuccessFlag) {
                host.setStatus(HostStatusEnum.READY);
                log.info(" == host check done, total success. == ");
            } else {
                host.setStatus(HostStatusEnum.ERROR);
                log.error(" == host check done, total failed! == ");
            }
            tableStorage.hostSave(host);

            // check vnc
            vncService.checkVncStatus();
        }
    }


    /**
     * 同步检查：适合用于启动自检
     * @param type 检查类型
     * @return 同步完成后直接返回结果，不直接改库
     */
    public boolean checkHostEnvSync(String type) {
        log.info(" ┏━ [{}] == start to check", type);
        if (!hostService.checkEnv(type)) {
            // return false: refers no need to run, so no need to check.
            log.info(" ┗━ SKIP: host checking done, no need to check [{}].", type);
            return true;
        }
        if (hostService.checkEnvRes(type)) {
            log.info(" ┗━ SUCCESS: host checking done, [{}] success.", type);
            return true;
        } else {
            log.warn(" ┗━ FAILED: host checking done, [{}] failed!", type);
            return false;
        }
    }


    /**
     * 异步检查：适合用于用户调用
     * @param type 检查类型
     * 异步执行修改数据库，不返回
     */
    public void checkHostEnvAsync(String type) {
        log.info(" ┏━ [{}] == start to check", type);
        if (!hostService.checkEnv(type)) {
            // return false: refers no need to run, so no need to check.
            log.info(" ┗━ SKIP: host checking done, no need to check [{}].", type);
            // write `READY` into DB
            hostService.setHostStatus(HostStatusEnum.READY);
            return;
        }
        new Thread(() -> {
            // get res in thread, then write DB host status.
            if (hostService.checkEnvRes(type)) {
                log.info(" ┗━ SUCCESS: host checking done, [{}] success.", type);
                // write `READY` into DB
                hostService.setHostStatus(HostStatusEnum.READY);
            } else {
                log.warn(" ┗━ FAILED: host checking done, [{}] failed!", type);
                // write `ERROR` into DB
                hostService.setHostStatus(HostStatusEnum.ERROR);
            }
        }).start();
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
