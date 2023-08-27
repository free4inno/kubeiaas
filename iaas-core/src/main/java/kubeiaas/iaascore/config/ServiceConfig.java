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

    /*
        static need map describes the node-specific deployment: the service's deployment is
        limited to the nodes provided by map value
      */
    private static final Map<String, List<String>> staticNeedMap = new HashMap<>();

    /*
        dynamic need set describes the node-independent deployment: the service's deployment is
        not bound to specific nodes
     */
    private static final Set<String> dynamicNeedSet = new HashSet<>();

    /* serviceList maintain active service */
    private static final List<Record> activeList = new ArrayList<>();

    /* init */
    ServiceConfig() {
        // 1. build static Need Map and dynamic Need Set
        staticNeedMap.put(ComponentConstants.DHCP,
                ConfigUtils.splitByComma(System.getenv("DHCP_NODE_NAME")));
        log.info("== SERVICE : staticNeedMap == \n" + staticNeedMap);

        dynamicNeedSet.add(ComponentConstants.IAAS_CORE);
        dynamicNeedSet.add(ComponentConstants.DB_PROXY);
        dynamicNeedSet.add(ComponentConstants.RESOURCE_OPERATOR);
        dynamicNeedSet.add(ComponentConstants.VNC);
        dynamicNeedSet.add(ComponentConstants.IMAGE_OPERATOR);
        dynamicNeedSet.add(ComponentConstants.LIBVIRT);
        dynamicNeedSet.add(ComponentConstants.IAAS_AGENT);
        log.info("== SERVICE : dynamicNeedSet == \n" + dynamicNeedSet);

        // 2. build Service Set
        serviceSet = new HashSet<>();
        serviceSet.addAll(staticNeedMap.keySet());
        serviceSet.addAll(dynamicNeedSet);
        log.info("== SERVICE : serviceSet == \n" + serviceSet);

        // 3. init service list
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
        log.debug("== SERVICE : activeList == \n" + activeList);
    }

    public void register(String svcName, String nodeName, Long timeStamp) {
        for (Record record : activeList) {
            if (svcName.equals(record.getSvcName()) && nodeName.equals(record.getNodeName())) {
                record.setLastTs(timeStamp);
                return;
            }
        }
        if (staticNeedMap.containsKey(svcName) && !staticNeedMap.get(svcName).contains(nodeName)) {
            log.warn("== SERVICE : newRegister(svcName:{}, nodeName:{}, ts:{}), but not in needMap",
                    svcName, nodeName, timeStamp);
        }
        activeList.add(new Record(svcName, nodeName, timeStamp));
        log.info("== SERVICE : newRegister(svcName:{}, nodeName:{}, ts:{})", svcName, nodeName, timeStamp);
    }

    public Map<String, List<String>> getSvc() {
        Map<String, List<String>> resMap = new HashMap<>();
        for (String svc : serviceSet) {
            List<String> nodeList = new ArrayList<>();
            for (Record record : activeList) {
                if (svc.equals(record.getSvcName())) {
                    nodeList.add(record.getNodeName());
                }
            }
            resMap.put(svc, nodeList);
        }
        return resMap;
    }

    /* Get active and dead service number */
    public Map<String, Integer> getSvcCount() {
        cleanServiceList();
        Map<String, Integer>resMap = new HashMap<>();
        int activeNum = 0,
            deadNum = 0;
        int svcCnt;
        for (String svc : dynamicNeedSet) {
            svcCnt = getSvcCnt(svc);
            if (svcCnt == 0) {
                deadNum += 1;
            } else {
                activeNum += svcCnt;
            }
        }
        // iterate staticNeedMap
        for (Map.Entry<String, List<String>> entry : staticNeedMap.entrySet()) {
            String svc = entry.getKey();
            for (String node : entry.getValue()) {
                if (isInActiveList(svc, node)) {
                    activeNum += 1;
                } else {
                    deadNum += 1;
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

    private boolean isInActiveList(String svcName, String nodeName) {
        for (Record record : activeList) {
            if (svcName.equals(record.getSvcName())
                    && nodeName.equals(record.getNodeName())) {
                return true;
            }
        }
        return false;
    }

    /* Get active service count by service name */
    private Integer getSvcCnt(String svcName) {
        int cnt = 0;
        for (Record record : activeList) {
            if (svcName.equals(record.getSvcName())) {
                cnt += 1;
            }
        }
        return cnt;
    }
}
