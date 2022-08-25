package kubeiaas.iaascore.dao;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Image;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.ImageConstants;
import kubeiaas.common.constants.RequestParamConstants;
import kubeiaas.iaascore.dao.feign.DbProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class TableStorage {

    @Resource
    private DbProxy dbProxy;

    public List<Vm> vmQueryAll() {
        String jsonString = dbProxy.vmQueryAll();
        return JSON.parseArray(jsonString, Vm.class);
    }

    public void vmSave(Vm vm) {
        String vmObjectStr = JSON.toJSONString(vm);
        dbProxy.vmSave(vmObjectStr);
    }

    public Image imageQueryByUuid(String uuid) {
        String jsonString = dbProxy.imageQueryAllBySingleKey(ImageConstants.UUID, uuid);
        List<Image> imageList = JSON.parseArray(jsonString, Image.class);
        if (imageList != null && !imageList.isEmpty()) {
            return imageList.get(0);
        } else {
            return null;
        }
    }

}
