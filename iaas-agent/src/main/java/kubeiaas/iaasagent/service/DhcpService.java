package kubeiaas.iaasagent.service;

import kubeiaas.common.bean.IpSegment;
import kubeiaas.common.utils.FileUtils;
import kubeiaas.common.utils.IpUtils;
import kubeiaas.common.utils.MacUtils;
import kubeiaas.common.utils.ShellUtils;
import kubeiaas.iaasagent.config.DhcpConfig;
import kubeiaas.iaasagent.config.HostConfig;
import kubeiaas.iaasagent.dao.TableStorage;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class DhcpService {

    @Resource
    private TableStorage tableStorage;

    /**
     * 根据网段更新 DHCP 监听配置
     */
    public boolean updateIpSeg(int ipSegId) {
        log.info(String.format("updateIpSeg info -- ipSegId: %d", ipSegId));
        // -------- 1. 获取 ipSegId 信息 --------
        log.info("STEP 1: get ipSegId info and check");

        log.info("<- invoke DB -- ipSegmentQueryById");
        IpSegment ipSegment = tableStorage.ipSegmentQueryById(ipSegId);
        log.info("<- invoke DB -- done");

        if (!ipSegment.getHostUuid().equals(HostConfig.thisHost.getUuid())) {
            log.warn(String.format("updateIpSeg -- ipSegId %d not belong to this host", ipSegId));
            return true;
        }

        // -------- 2. 构造配置串 --------
        log.info("STEP 2: construct dhcp config line");
        String segName = "IP_SEG_" + ipSegment.getBridge();
        String subnetIP = IpUtils.getSubnet(ipSegment.getIpRangeStart(), ipSegment.getNetmask());
        String confLine = String.format(DhcpConfig.BIND_IP_SEG_TEMPLATE,
                segName, ipSegment.getBridge(), subnetIP, ipSegment.getNetmask(), ipSegment.getGateway());

        // -------- 3. 更新 dhcp conf --------
        log.info("STEP 3: update dhcp config file");
        try {
            // 先拷贝旧文件
            FileUtils.copy(DhcpConfig.DHCP_CONF_FILE_PATH, DhcpConfig.DHCP_CONF_FILE_TEMP_PATH);

            // 删除旧有（sed帮助判定）
            String cmdDefault = "sed -i '/" + "default" + "/d' " + DhcpConfig.DHCP_CONF_FILE_PATH;
            ShellUtils.getCmd(cmdDefault);

            String cmd = "sed -i '/" + segName + "/d' " + DhcpConfig.DHCP_CONF_FILE_PATH;
            ShellUtils.getCmd(cmd);

            // 再追加新内容
            FileWriter writer = new FileWriter(DhcpConfig.DHCP_CONF_FILE_PATH, true);
            writer.write(DhcpConfig.ESCAPE_NEWLINE + confLine);
            writer.close();

        } catch (Exception e) {
            log.error("ERROR: IO dhcp config files failed!");
            return false;
        }

        // -------- 4. 重启 dhcp server --------
        log.info("STEP 4: restart dhcp server");
        if (!restartDHCP()) {
            log.error("ERROR: restart dhcp server error!");
            return false;
        }
        return true;
    }

    /**
     * 绑定 dhcp 配置中的 mac 与 ip
     * @param vmUuid 待绑定虚拟机 Uuid
     * @param mac 待绑定 mac 地址
     * @param ip 待绑定 ip 地址
     * @return 绑定结果
     */
    public boolean bindMacAndIp(String vmUuid, String mac, String ip) {
        log.info(String.format("bindMacAndIp info -- vmUuid: %s, mac: %s, ip: %s", vmUuid, mac, ip));
        // -------- 1. 打开配置文件 --------
        log.info("STEP 1: open dhcp config file");
        File dhcpConfigFile = new File(DhcpConfig.DHCP_CONF_FILE_PATH);
        if (!dhcpConfigFile.exists()) {
            log.error(String.format("bindMacAndIp -- dhcp config file %s not exist!", DhcpConfig.DHCP_CONF_FILE_PATH));
            return false;
        }

        // -------- 2. 构建文件内容 --------
        log.info("STEP 2: construct dhcp config content");
        // 定义宿主机在 dhcp config 中的名称
        String hostName = vmUuid + MacUtils.deleteAllColon(mac);

        // 获取 dhcp config 内容模板
        String template = DhcpConfig.BIND_VM_TEMPLATE;

        // 构建模板参数列表（map 的 key 为和模版中 ${} 内的值一致 value 为要替换的结果）
        Map<String, Object> params = new HashMap<>();
        params.put(DhcpConfig.BIND_VM_TEMPLATE_PARAM_HOSTNAME, hostName);
        params.put(DhcpConfig.BIND_VM_TEMPLATE_PARAM_IP, ip);
        params.put(DhcpConfig.BIND_VM_TEMPLATE_PARAM_MAC, mac);

        // 替换得到 content
        StringWriter stringWriter = new StringWriter();
        Velocity.evaluate(new VelocityContext(params), stringWriter, DhcpConfig.BIND_VM_TEMPLATE_LOG_TAG, template);        //模版引起开始替换模版内容
        String newBindContent = stringWriter.getBuffer().toString();
        log.debug("bindMacAndIp -- new Mac and Ip bind Content: " + newBindContent);

        // -------- 3. 读写文件 --------
        log.info("STEP 3: read and write dhcp config file");
        try {
            // 先拷贝旧文件
            FileUtils.copy(DhcpConfig.DHCP_CONF_FILE_PATH, DhcpConfig.DHCP_CONF_FILE_TEMP_PATH);

            // 再追加新内容
            FileWriter writer = new FileWriter(DhcpConfig.DHCP_CONF_FILE_PATH, true);
            writer.write(DhcpConfig.ESCAPE_NEWLINE + newBindContent);
            writer.close();
        } catch (Exception e) {
            log.error("ERROR: IO dhcp config files failed!");
            return false;
        }

        // -------- 4. 重启 dhcp server --------
        if (!restartDHCP()) {
            log.error("ERROR: restart dhcp server error!");
            return false;
        }
        return true;
    }

    /**
     * 解绑待删除虚拟机mac地址和其Ip地址
     */
    public boolean unbindMacAndIp(String instanceUuid){
        log.info(String.format("unbindMacAndIp info -- instanceUuid: %s", instanceUuid));
        if (instanceUuid == null || instanceUuid.isEmpty()) {
            log.error("ERROR: restartDHCP -- DHCP Controller unbind mac and ip error, Because mac and ip params is null");
            return false;
        }
        String cmd = "sed -i '/" + instanceUuid + "/d' " + DhcpConfig.DHCP_CONF_FILE_PATH;
        ShellUtils.getCmd(cmd);
        if (!restartDHCP()) {
            log.error("ERROR: unbindMacAndIp -- restart dhcp server error!");
            return false;
        }
        return true;
    }

    // ================================================================================================================

    /**
     * 重启本机上的 dhcp 服务
     * @return 重启结果
     */
    private boolean restartDHCP() {
        return ShellUtils.run(DhcpConfig.DHCP_RESTART_CMD);
    }
}
