package kubeiaas.resourceoperator.dao;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.Image;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.HostConstants;
import kubeiaas.common.constants.ImageConstants;
import kubeiaas.common.constants.VmConstants;
import kubeiaas.resourceoperator.dao.feign.DbProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class TableStorage {

    @Resource
    private DbProxy dbProxy;

    // ========================= vm =========================

    public List<Vm> vmQueryAll() {
        String jsonString = dbProxy.vmQueryAll();
        return JSON.parseArray(jsonString, Vm.class);
    }

    public Vm vmQueryByUuid(String uuid) {
        String jsonString = dbProxy.vmQueryAllBySingleKey(VmConstants.UUID, uuid);
        List<Vm> vmList = JSON.parseArray(jsonString, Vm.class);
        if (vmList != null && !vmList.isEmpty()) {
            return vmList.get(0);
        } else {
            return null;
        }
    }

    public void vmSave(Vm vm) {
        String vmObjectStr = JSON.toJSONString(vm);
        dbProxy.vmSave(vmObjectStr);
    }

    // ========================= image =========================

    public Image imageQueryByUuid(String uuid) {
        String jsonString = dbProxy.imageQueryAllBySingleKey(ImageConstants.UUID, uuid);
        List<Image> imageList = JSON.parseArray(jsonString, Image.class);
        if (imageList != null && !imageList.isEmpty()) {
            return imageList.get(0);
        } else {
            return null;
        }
    }

    // ========================= host =========================

    public List<Host> hostQueryAll() {
        String jsonString = dbProxy.hostQueryAll();
        return JSON.parseArray(jsonString, Host.class);
    }

    public Host hostQueryByUuid(String uuid) {
        String jsonString = dbProxy.hostQueryAllBySingleKey(HostConstants.UUID, uuid);
        List<Host> hostList = JSON.parseArray(jsonString, Host.class);
        if (hostList != null && !hostList.isEmpty()) {
            return hostList.get(0);
        } else {
            return null;
        }
    }

}
