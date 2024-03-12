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
            log.error("ERROR: createVmInstance. ", e);
            return false;
        }
    }

    public boolean createVmInstanceByXml(String vmUuid, String xmlDesc) {
        return vmController.createVmInstanceByXml(getSelectedUri(vmUuid), vmUuid, xmlDesc).equals(ResponseMsgConstants.SUCCESS);
    }

    public boolean deleteVmInstance(String vmUuid){
        try {
            return vmController.deleteVmInstance(getSelectedUri(vmUuid), vmUuid).equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error("ERROR: deleteVmInstance. ", e);
            return false;
        }
    }

    public boolean stopVmInstance(String vmUuid){
        try {
            return vmController.stopVmInstance(getSelectedUri(vmUuid), vmUuid)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error("ERROR: stopVmInstance. ", e);
            return false;
        }
    }

    public boolean startVmInstance(String vmUuid){
        try {
            return vmController.startVmInstance(getSelectedUri(vmUuid), vmUuid)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error("ERROR: startVmInstance. ", e);
            return false;
        }
    }

    public boolean rebootVmInstance(String vmUuid){
        try {
            return vmController.rebootVmInstance(getSelectedUri(vmUuid), vmUuid)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error("ERROR: rebootVmInstance. ", e);
            return false;
        }
    }

    public boolean suspendVmInstance(String vmUuid){
        try {
            return vmController.suspendVmInstance(getSelectedUri(vmUuid), vmUuid)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error("ERROR: suspendVmInstance. ", e);
            return false;
        }
    }

    public boolean resumeVmInstance(String vmUuid){
        try {
            return vmController.resumeVmInstance(getSelectedUri(vmUuid), vmUuid)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error("ERROR: resumeVmInstance. ", e);
            return false;
        }
    }


    public boolean modifyVmInstance(String vmUuid, Integer cpus, Integer memory) {
        try {
            return vmController.modifyVmInstance(getSelectedUri(vmUuid), vmUuid, cpus, memory)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error("ERROR: modifyVmInstance. ", e);
            return false;
        }
    }

    public boolean volumePublishImage(String vmUuid, String imagePath, String volumePath, Integer extraSize) {
        try {
            return volumeController.volumePublishImage(getSelectedUri(vmUuid), imagePath, volumePath, extraSize)
                    .equals(ResponseMsgConstants.SUCCESS);
        } catch (Exception e) {
            log.error("ERROR: volumePublishImage. ", e);
            return false;
        }
    }

    public String getXmlDesc(Vm vm) throws BaseException {
        try {
            return vmController.getXmlDesc(getSelectedUri(vm.getUuid()), vm.getUuid());
        } catch (Exception e) {
            log.error("ERROR: getXmlDesc. ", e);
            throw new BaseException("err: failed to connect with agent.");
        }
    }

    public VmStatusEnum getStatus(Vm vm) throws BaseException {
        VmStatusEnum vmStatusEnum = null;
        try {
            String statusStr = vmController.status(getSelectedUri(vm.getUuid()), vm.getUuid());
            vmStatusEnum = JSON.parseObject(statusStr, VmStatusEnum.class);
        } catch (Exception e) {
            log.error("ERROR: getStatus.", e);
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
            log.error("ERROR: setPasswd. ", e);
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
            log.error("ERROR: build URI failed! %s", e);
            return null;
        }
    }

}
