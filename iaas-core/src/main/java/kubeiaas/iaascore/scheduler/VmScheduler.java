package kubeiaas.iaascore.scheduler;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Image;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.dao.feign.ImageOperator;
import kubeiaas.iaascore.dao.feign.VmController;
import kubeiaas.iaascore.dao.feign.VolumeController;
import kubeiaas.iaascore.process.MountProcess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@Configuration
public class VmScheduler {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private VmController vmController;

    @Resource
    private VolumeController volumeController;

    @Resource
    private MountProcess mountProcess;

    public boolean createVmInstance(String vmUuid) {
        try {
            List<Volume> volumeList = tableStorage.volumeQueryAllByInstanceUuid(vmUuid);
            Vm vm = tableStorage.vmQueryByUuid(vmUuid);
            // 1. 挂载 volume
            if (!mountProcess.attachVolumes(volumeList, vm)) {
                return false;
            }
            // 2. 调用 agent 执行 create
            vmController.createVmInstance(getSelectedUri(vmUuid), vmUuid);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteVmInstance(String vmUuid){
        try {
            return vmController.deleteVmInstance(getSelectedUri(vmUuid), vmUuid).equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean stopVmInstance(String vmUuid){
        try {
            return vmController.stopVmInstance(getSelectedUri(vmUuid), vmUuid)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean startVmInstance(String vmUuid){
        try {
            return vmController.startVmInstance(getSelectedUri(vmUuid), vmUuid)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean rebootVmInstance(String vmUuid){
        try {
            return vmController.rebootVmInstance(getSelectedUri(vmUuid), vmUuid)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean suspendVmInstance(String vmUuid){
        try {
            return vmController.suspendVmInstance(getSelectedUri(vmUuid), vmUuid)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resumeVmInstance(String vmUuid){
        try {
            return vmController.resumeVmInstance(getSelectedUri(vmUuid), vmUuid)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean modifyVmInstance(String vmUuid, Integer cpus, Integer memory) {
        try {
            return vmController.modifyVmInstance(getSelectedUri(vmUuid), vmUuid, cpus, memory)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean volumePublishImage(String vmUuid, String imagePath, String volumePath, Image image) {
        String imageObjectStr = JSON.toJSONString(image);
        try {
            return volumeController.volumePublishImage(getSelectedUri(vmUuid), imagePath, volumePath, imageObjectStr)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteVmInDataBase(String vmUuid){
        tableStorage.vmDeleteByUuid(vmUuid);
    }

    private URI getSelectedUri(String vmUuid) {
        try {
            return new URI(AgentConfig.getSelectedUri(vmUuid));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            log.error("build URI failed!");
            return null;
        }
    }

}
