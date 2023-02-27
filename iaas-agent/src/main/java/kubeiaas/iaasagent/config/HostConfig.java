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
import java.util.*;

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

    private final Map<String ,String> roleNodes = new HashMap<>();
    HostConfig() {
        roleNodes.put(HostConstants.ROLE_DHCP, System.getenv("DHCP_NODE"));
        roleNodes.put(HostConstants.ROLE_VNC, System.getenv("VNC_NODE"));
        roleNodes.put(HostConstants.ROLE_NFS, System.getenv("NFS_NODE"));
    }

    private final List<String> agentList = this.splitByComma(System.getenv("AGENT_NODE_LIST"));
    private final List<String> vCPU_LIST = this.splitByComma(System.getenv("AGENT_VCPUS_LIST"));
    private final List<String> MEM_LIST = this.splitByComma(System.getenv("AGENT_MEM_LIST"));
    private final List<String> STORAGE_LIST = this.splitByComma(System.getenv("AGENT_STORAGE_LIST"));

    private String vCPU = "0";
    private String MEM = "0";
    private String STORAGE = "0";

    public static String CMD_CPU_CORE = "cat /proc/cpuinfo| grep \"processor\" | wc -l";
    public static String CMD_CPU_MHZ = "cat /proc/cpuinfo | grep MHz|head -1|awk '{print $4}'";
    public static String CMD_MEM_GB = "cat /proc/meminfo | grep MemTotal | awk '{print $2/1024/1024}'";
    public static String CMD_DISK_GB = "df | grep '/$' | awk '{print int($2*1/1024/1024)}'";
    public static String CMD_VERSION = "cat /proc/version";

    public static String RESULT_PREPARE = "/usr/local/kubeiaas/workdir/log/prepare_result.log";

    public void hostInitialize() {
        // get host resource config
        log.info("hostIp:" + hostIp + ", hostName:" + hostName);

        int index = 0;
        for (String agent : agentList) {
            if (agent.equals(hostName) || agent.equals(hostIp)) {
                vCPU = vCPU_LIST.get(index);
                MEM = MEM_LIST.get(index);
                STORAGE = STORAGE_LIST.get(index);
            }
            index += 1;
        }
        log.info("vCPU:" + vCPU + ", MEM:" + MEM + ", STORAGE:" + STORAGE);

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

            // - generate uuid
            host.setUuid(UuidUtils.getRandomUuid());

            // save into DB
            host = tableStorage.hostSave(host);
            log.info(JSON.toJSONString(host));

        } else {
            log.info("this host is registered.");
        }

        // - set resource config
        host.setVCPU(Integer.parseInt(vCPU));
        host.setMemory(Integer.parseInt(MEM));
        host.setStorage(Integer.parseInt(STORAGE));

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
            log.info(" == host check done, total success. == ");
        } else {
            host.setStatus(HostStatusEnum.ERROR);
            log.error(" == host check done, total failed! == ");
        }
        tableStorage.hostSave(host);
    }

    private List<String> splitByComma(String str) {
        return Arrays.asList(str.split(","));
    }

    public String getHostIp() {
        return hostIp;
    }

    public String getHostName() {
        return hostName;
    }
}
