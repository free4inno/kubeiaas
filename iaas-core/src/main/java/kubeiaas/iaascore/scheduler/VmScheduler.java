package kubeiaas.iaascore.scheduler;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Image;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.dao.feign.VmController;
import kubeiaas.iaascore.dao.feign.VolumeController;
import kubeiaas.iaascore.exception.BaseException;
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
            log.error(String.format("ERROR: createVmInstance. Exception: %s", e));
            return false;
        }
    }

    public boolean deleteVmInstance(String vmUuid){
        try {
            return vmController.deleteVmInstance(getSelectedUri(vmUuid), vmUuid).equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error(String.format("ERROR: deleteVmInstance. Exception: %s", e));
            return false;
        }
    }

    public boolean stopVmInstance(String vmUuid){
        try {
            return vmController.stopVmInstance(getSelectedUri(vmUuid), vmUuid)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error(String.format("ERROR: stopVmInstance. Exception: %s", e));
            return false;
        }
    }

    public boolean startVmInstance(String vmUuid){
        try {
            return vmController.startVmInstance(getSelectedUri(vmUuid), vmUuid)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error(String.format("ERROR: startVmInstance. Exception: %s", e));
            return false;
        }
    }

    public boolean rebootVmInstance(String vmUuid){
        try {
            return vmController.rebootVmInstance(getSelectedUri(vmUuid), vmUuid)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error(String.format("ERROR: rebootVmInstance. Exception: %s", e));
            return false;
        }
    }

    public boolean suspendVmInstance(String vmUuid){
        try {
            return vmController.suspendVmInstance(getSelectedUri(vmUuid), vmUuid)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error(String.format("ERROR: suspendVmInstance. Exception: %s", e));
            return false;
        }
    }

    public boolean resumeVmInstance(String vmUuid){
        try {
            return vmController.resumeVmInstance(getSelectedUri(vmUuid), vmUuid)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error(String.format("ERROR: resumeVmInstance. Exception: %s", e));
            return false;
        }
    }


    public boolean modifyVmInstance(String vmUuid, Integer cpus, Integer memory) {
        try {
            return vmController.modifyVmInstance(getSelectedUri(vmUuid), vmUuid, cpus, memory)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error(String.format("ERROR: modifyVmInstance. Exception: %s", e));
            return false;
        }
    }

    public boolean volumePublishImage(String vmUuid, String imagePath, String volumePath, Integer extraSize) {
        try {
            return volumeController.volumePublishImage(getSelectedUri(vmUuid), imagePath, volumePath, extraSize)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error(String.format("ERROR: volumePublishImage. Exception: %s", e));
            return false;
        }
    }

    public VmStatusEnum getStatus(Vm vm) throws BaseException {
        VmStatusEnum vmStatusEnum = null;
        try {
            String statusStr = vmController.status(getSelectedUri(vm.getUuid()), vm.getUuid());
            vmStatusEnum = JSON.parseObject(statusStr, VmStatusEnum.class);
        } catch (Exception e) {
            log.error(String.format("ERROR: getStatus. Exception: %s", e));
        }
        if (null == vmStatusEnum) {
            log.error(String.format("ERROR: failed to get current vm status, uuid %s",vm.getUuid()));
            throw new BaseException("err: failed to get current vm status, uuid " + vm.getUuid());
        }
        return vmStatusEnum;
    }

    public boolean setPasswd(Vm vm, String user, String passwd) throws BaseException {
        try {
            return vmController.setPasswd(getSelectedUri(vm.getUuid()), vm.getUuid(), user, passwd)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error(String.format("ERROR: setPasswd. Exception: %s", e));
            throw new BaseException("err: failed to connect with agent.");
        }
    }

    public void deleteVmInDataBase(String vmUuid){
        log.info(String.format("-> invoke DB -- vmDeleteByUuid, uuid: %s", vmUuid));
        tableStorage.vmDeleteByUuid(vmUuid);
        log.info("<- invoke DB -- done");
    }

    private URI getSelectedUri(String vmUuid) {
        try {
            return new URI(AgentConfig.getSelectedUri(vmUuid));
        } catch (URISyntaxException e) {
            log.error(String.format("ERROR: build URI failed! %s", e));
            return null;
        }
    }

}
