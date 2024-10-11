package kubeiaas.iaascore.service;

import com.alibaba.fastjson2.JSON;
import kubeiaas.common.bean.*;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.IpSegmentConstants;
import kubeiaas.common.enums.network.IpTypeEnum;
import kubeiaas.common.utils.IpUtils;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.process.NetworkProcess;
import kubeiaas.iaascore.request.ipSegment.CreateIpSegmentForm;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class HostBridgeService {
    @Resource
    private TableStorage tableStorage;

    /**
     * 拆分ipSegment表后迁移数据
     */
    public String migrateFromIpSegment() {
        log.info("-> invoke DB -- migrateIpSegment -- save");
        tableStorage.hostBridgeMigrateFromIpSegment();
//        HostBridge hostBridge = new HostBridge();
//        hostBridge.setId(null);
//        hostBridge.setHostUuid("58b94fc0197f43c5bec07fe69be4b9c0");
//        hostBridge.setBridgeName("eno1");
//        hostBridge.setIpSegmentId(68);
//        hostBridge.setHostName("hf-node-02");
//        tableStorage.hostBridgeSave(hostBridge);
        log.info("<- invoke DB -- done");
        return ResponseMsgConstants.SUCCESS;
    }

    public List<HostBridge> queryAllHostBridge() {
        log.info("-> invoke DB -- hostBridgeQueryAll");
        List<HostBridge> hostBridgeList = tableStorage.hostBridgeQueryAll();
        log.info("<- invoke DB -- done");
        return hostBridgeList;
    }


}
