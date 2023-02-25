package kubeiaas.iaasagent.service;

import kubeiaas.common.bean.IpSegment;
import kubeiaas.common.utils.FileUtils;
import kubeiaas.common.utils.IpUtils;
import kubeiaas.common.utils.MacUtils;
import kubeiaas.common.utils.ShellUtils;
import kubeiaas.iaasagent.config.DhcpConfig;
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

@Slf4j
@Service
public class DhcpService {

    @Resource
    private TableStorage tableStorage;

    /**
     * 根据网段更新 DHCP 监听配置
     */
    public boolean updateIpSeg(int ipSegId) {
        // -------- 1. 获取 ipSegId 信息 --------
        IpSegment ipSegment = tableStorage.ipSegmentQueryById(ipSegId);

        // -------- 2. 构造配置串 --------
        String segName = "IP_SEG_" + ipSegment.getBridge();
        String subnetIP = IpUtils.getSubnet(ipSegment.getIpRangeStart(), ipSegment.getNetmask());
        String confLine = String.format(DhcpConfig.BIND_IP_SEG_TEMPLATE,
                segName, ipSegment.getBridge(), subnetIP, ipSegment.getNetmask(), ipSegment.getGateway());

        // -------- 3. 更新 dhcp conf --------
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
            log.error("bindMacAndIp -- IO dhcp config files failed!");
            e.printStackTrace();
            return false;
        }

        // -------- 4. 重启 dhcp server --------
        if (!restartDHCP()) {
            log.error("updateIpSeg -- restart dhcp server error!");
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
        // -------- 1. 打开配置文件 --------
        File dhcpConfigFile = new File(DhcpConfig.DHCP_CONF_FILE_PATH);
        if (!dhcpConfigFile.exists()) {
            log.error("bindMacAndIp -- Cannot find DHCP config file in path: " + DhcpConfig.DHCP_CONF_FILE_PATH);
            return false;
        }

        // -------- 2. 构建文件内容 --------
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
        log.info("bindMacAndIp -- new Mac and Ip bind Content: " + newBindContent);

        // -------- 3. 读写文件 --------
        try {
            // 先拷贝旧文件
            FileUtils.copy(DhcpConfig.DHCP_CONF_FILE_PATH, DhcpConfig.DHCP_CONF_FILE_TEMP_PATH);

            // 再追加新内容
            FileWriter writer = new FileWriter(DhcpConfig.DHCP_CONF_FILE_PATH, true);
            writer.write(DhcpConfig.ESCAPE_NEWLINE + newBindContent);
            writer.close();
        } catch (Exception e) {
            log.error("bindMacAndIp -- IO dhcp config files failed!");
            e.printStackTrace();
            return false;
        }

        // -------- 4. 重启 dhcp server --------
        if (!restartDHCP()) {
            log.error("bindMacAndIp -- restart dhcp server error!");
            return false;
        }
        return true;
    }

    /**
     * 解绑待删除虚拟机mac地址和其Ip地址
     */
    public boolean unbindMacAndIp(String instanceUuid){
        log.info("unbindMacAndIp ---- start ---- instanceUuid: " + instanceUuid);
        if (instanceUuid == null || instanceUuid.equals("")) {
            log.error("restartDHCP -- DHCP Controller unbind mac and ip error, Because mac and ip params is null");
            return false;
        }
        String cmd = "sed -i '/" + instanceUuid + "/d' " + DhcpConfig.DHCP_CONF_FILE_PATH;
        ShellUtils.getCmd(cmd);
        if (!restartDHCP()) {
            log.error("unbindMacAndIp -- restart dhcp server error!");
            return false;
        }
        log.info("unbindMacAndIp ---- end ----");
        return true;
    }

    // ================================================================================================================

    /**
     * 重启本机上的 dhcp 服务
     * @return 重启结果
     */
    private boolean restartDHCP() {
        if (!ShellUtils.run(DhcpConfig.DHCP_RESTART_CMD)) {
            log.error("restartDHCP -- restart dhcp server error! please check it.");
            return false;
        } else {
            return true;
        }
    }
}
