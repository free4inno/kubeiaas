package kubeiaas.iaascore.config;

import kubeiaas.common.constants.ComponentConstants;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.service.ServiceStatusEnum;
import kubeiaas.iaascore.utils.ConfigUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;

@Slf4j
@Configuration
public class ServiceConfig {

    /* 服务注册记录 */
    @Data
    @AllArgsConstructor
    private static class Record {
        private String svcName;
        private String nodeName;
        private Long lastTs;
    }

    /* env */
    private final String HOST_IP = System.getenv("HOST_IP");
    private final String HOST_NAME = System.getenv("HOST_NAME");
    private final String NFS_IP = System.getenv("NFS_IP");

    /* Set */
    private final Set<String> serviceSet;
    private final Set<String> nodeSet;

    /* Need Map */
    private static final Map<String, List<String>> needMap = new HashMap<>();

    /* serviceList */
    private static final List<Record> activeList = new ArrayList<>();

    /* init */
    ServiceConfig() {
        // 1. build Need Map
        needMap.put(ComponentConstants.IAAS_CORE,
                ConfigUtils.splitByComma(System.getenv("IAAS_CORE_NODE_NAME")));
        needMap.put(ComponentConstants.DB_PROXY,
                ConfigUtils.splitByComma(System.getenv("DB_PROXY_NODE_NAME")));
        needMap.put(ComponentConstants.RESOURCE_OPERATOR,
                ConfigUtils.splitByComma(System.getenv("RS_OPT_NODE_NAME")));
        needMap.put(ComponentConstants.IMAGE_OPERATOR,
                ConfigUtils.splitByComma(System.getenv("IMG_OPT_NODE_NAME")));
        needMap.put(ComponentConstants.IAAS_AGENT,
                ConfigUtils.splitByComma(System.getenv("AGENT_NODE_NAME_LIST")));
        needMap.put(ComponentConstants.LIBVIRT,
                ConfigUtils.splitByComma(System.getenv("AGENT_NODE_NAME_LIST")));
        needMap.put(ComponentConstants.DHCP,
                ConfigUtils.splitByComma(System.getenv("DHCP_NODE_NAME")));
        needMap.put(ComponentConstants.VNC,
                ConfigUtils.splitByComma(System.getenv("VNC_NODE_NAME")));
        log.info("== SERVICE : needMap == \n" + needMap);

        // 2. build Service Set
        serviceSet = needMap.keySet();
        log.info("== SERVICE : serviceSet == \n" + serviceSet);

        // 3. build Node Set
        nodeSet = new HashSet<>();
        for (String svc : serviceSet) {
            nodeSet.addAll(needMap.get(svc));
        }
        log.info("== SERVICE : nodeSet == \n" + nodeSet);

        // 4. init service list
        activeList.add(new Record(ComponentConstants.IAAS_CORE, this.HOST_NAME, System.currentTimeMillis()));
    }

    /* 定时任务检查注册信息超时 */
    @Scheduled(cron = "0 0/1 * * * ?") // every 1 min
    private void cleanServiceList() {
        Long timeNow = System.currentTimeMillis();
        register(ComponentConstants.IAAS_CORE, this.HOST_NAME, timeNow);
        // 清理超过 2T = 2 * 1min = 2min 的注册记录
        // -> 2 * 60 * 1000 = 120000
        activeList.removeIf(record -> (timeNow - record.getLastTs() > 120000));
        log.info("== SERVICE : activeList == \n" + activeList);
    }

    public void register(String svcName, String nodeName, Long timeStamp) {
        for (Record record : activeList) {
            if (svcName.equals(record.getSvcName()) && nodeName.equals(record.getNodeName())) {
                record.setLastTs(timeStamp);
                return;
            }
        }
        activeList.add(new Record(svcName, nodeName, timeStamp));
        log.info("== SERVICE : newRegister(svcName:{}, nodeName:{}, ts:{})", svcName, nodeName, timeStamp);
    }

    public Map<String, Map<String, ServiceStatusEnum>> getSvc() {
        Map<String, Map<String, ServiceStatusEnum>> resMap = new HashMap<>();
        for (String node : nodeSet) {
            Map<String, ServiceStatusEnum> rowMap = new HashMap<>();
            for (String svc : serviceSet) {
                if (isInNeedMap(svc, node)) {
                    if (isInActiveList(svc, node)) {
                        rowMap.put(svc, ServiceStatusEnum.ACTIVE);
                    } else {
                        rowMap.put(svc, ServiceStatusEnum.DEAD);
                    }
                } else {
                    if (isInActiveList(svc, node)) {
                        rowMap.put(svc, ServiceStatusEnum.NON);
                    } else {
                        rowMap.put(svc, ServiceStatusEnum.NON);
                    }
                }
            }
            resMap.put(node, rowMap);
        }
        return resMap;
    }

    public Map<String, Integer> getSvcCount() {
        int activeNum = 0;
        int deadNum = 0;
        Map<String, Integer> resMap = new HashMap<>();
        for (String node : nodeSet) {
            for (String svc : serviceSet) {
                if (isInNeedMap(svc, node)) {
                    if (isInActiveList(svc, node)) {
                        activeNum += 1;
                    } else {
                        deadNum += 1;
                    }
                }
            }
        }
        resMap.put("total", activeNum + deadNum);
        resMap.put("active", activeNum);
        return resMap;
    }

    public boolean getAgent(String nodeName) {
        return isInActiveList(ComponentConstants.IAAS_AGENT, nodeName);
    }

    public Map<String, String> getNfs() {
        Map<String, String> resMap = new HashMap<>();

        // 1. get NFS IP
        resMap.put(ComponentConstants.NFS_IP, this.NFS_IP);

        // 2. get NFS DIR
        List<String> dirList = new ArrayList<>();
        dirList.add(VolumeConstants.DEFAULT_NFS_SRV_PATH + VolumeConstants.DATA_VOLUME_PATH);
        dirList.add(VolumeConstants.DEFAULT_NFS_SRV_PATH + VolumeConstants.IMAGE_PATH);
        resMap.put(ComponentConstants.NFS_DIR, dirList.toString());

        return resMap;
    }

    // -----------------------------------------------------------------------------------

    private boolean isInNeedMap(String svcName, String nodeName) {
        List<String> nodes = needMap.get(svcName);
        return nodes.contains(nodeName);
    }

    private boolean isInActiveList(String svcName, String nodeName) {
        for (Record record : activeList) {
            if (svcName.equals(record.getSvcName())
                    && nodeName.equals(record.getNodeName())) {
                return true;
            }
        }
        return false;
    }
}
