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
import java.util.*;

@Slf4j
@Configuration
public class HostConfig {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private HostService hostService;

    public static Host thisHost;

    private final String hostIp = System.getenv("HOST_IP");
    private final String hostName = System.getenv("HOST_NAME");

    public String getHostIp() {
        return hostIp;
    }
    public String getHostName() {
        return hostName;
    }

    private final Map<String ,String> roleNodes = new HashMap<>();
    HostConfig() {
        roleNodes.put(HostConstants.ROLE_DHCP, System.getenv("DHCP_NODE"));
        roleNodes.put(HostConstants.ROLE_VNC, System.getenv("VNC_NODE"));
        roleNodes.put(HostConstants.ROLE_NFS, System.getenv("NFS_NODE"));
    }

    private static final int DEFAULT_vCPU = 0;
    private static final int DEFAULT_MEM = 0;
    private static final int DEFAULT_STORAGE = 0;

    public static String CMD_CPU_CORE = "cat /proc/cpuinfo| grep \"processor\" | wc -l";
    public static String CMD_CPU_MHZ = "cat /proc/cpuinfo | grep MHz|head -1|awk '{print $4}'";
    public static String CMD_MEM_GB = "cat /proc/meminfo | grep MemTotal | awk '{print $2/1024/1024}'";
    public static String CMD_DISK_GB = "df | grep '/$' | awk '{print int($2*1/1024/1024)}'";
    public static String CMD_VERSION = "cat /proc/version";

    public static String RESULT_PREPARE = "/usr/local/kubeiaas/workdir/log/prepare_result.log";

    public void hostInitialize() {
        // get host resource config
        log.info("hostIp:" + hostIp + ", hostName:" + hostName);

        // check is this host registered in DB
        Host host = tableStorage.hostQueryByIp(hostIp);
        if (host == null) {
            // do register
            log.info("this host is not registered! Start to register.");
            host = new Host();
            host.setIp(hostIp);
            host.setName(hostName);

            // - set Role (empty)
            JSONArray roleArray = new JSONArray();
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
            host.setVCPU(DEFAULT_vCPU);
            host.setMemory(DEFAULT_MEM);
            host.setStorage(DEFAULT_STORAGE);

            // - generate uuid
            host.setUuid(UuidUtils.getRandomUuid());

            // save into DB
            host = tableStorage.hostSave(host);
            log.info(JSON.toJSONString(host));

        } else {
            log.info("this host is registered.");
        }

        // - set roles
        List<String> roleList = new ArrayList<>();
        for (String role : roleNodes.keySet()) {
            String node = roleNodes.get(role);
            if (node.equals(hostIp) || node.equals(hostName)) {
                roleList.add(role);
            }
        }
        host.setRole(JSON.toJSONString(roleList));

        // - check env prepare status
        boolean totalSuccessFlag;
        totalSuccessFlag = hostService.getEnvPrepareRes(roleList);
        if (totalSuccessFlag) {
            host.setStatus(HostStatusEnum.READY);
            log.info("== host check done, total success. == ");
        } else {
            host.setStatus(HostStatusEnum.ERROR);
            log.error("== host check done, total failed! == ");
        }

        // - save
        tableStorage.hostSave(host);
        thisHost = host;
    }

    private List<String> splitByComma(String str) {
        return Arrays.asList(str.split(","));
    }

}
