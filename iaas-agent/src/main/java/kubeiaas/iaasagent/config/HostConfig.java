package kubeiaas.iaasagent.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import kubeiaas.common.bean.Host;
import kubeiaas.common.enums.host.HostStatusEnum;
import kubeiaas.common.utils.ShellUtils;
import kubeiaas.common.utils.UuidUtils;
import kubeiaas.iaasagent.dao.TableStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Slf4j
@Configuration
public class HostConfig {

    @Resource
    private TableStorage tableStorage;

    public static String CMD_CPU_CORE = "cat /proc/cpuinfo| grep \"processor\" | wc -l";
    public static String CMD_CPU_MHZ = "cat /proc/cpuinfo | grep MHz|head -1|awk '{print $4}'";
    public static String CMD_MEM_GB = "cat /proc/meminfo | grep MemTotal | awk '{print $2/1024/1024}'";
    public static String CMD_DISK_GB = "df | grep '/$' | awk '{print int($2*1/1024/1024)}'";
    public static String CMD_VERSION = "cat /etc/redhat-release";

    public void hostRegister() {
        String hostIp = "";
        String hostName = "";

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
            // - set Role (empty)
            JSONArray jsonArray = new JSONArray();
            host.setRole(jsonArray.toJSONString());
            // - set status
            host.setStatus(HostStatusEnum.PREPARING);
            // - set config
            String cpuCore = ShellUtils.getCmd(CMD_CPU_CORE);
            String cpuMhz = ShellUtils.getCmd(CMD_CPU_MHZ);
            String diskSize = ShellUtils.getCmd(CMD_DISK_GB);
            String memSize = ShellUtils.getCmd(CMD_MEM_GB);
            String os = ShellUtils.getCmd(CMD_VERSION);
            host.setConfig(String.format("系统版本：%s；处理器：%s核心，%sMHz；内存：%sGB；磁盘：%sGB。", os, cpuCore, cpuMhz, diskSize, memSize));
            // - generate uuid
            host.setUuid(UuidUtils.getRandomUuid());
            log.info(JSON.toJSONString(host));

            // save into DB

        } else {
            log.info("this host is registered.");
        }
    }
}
