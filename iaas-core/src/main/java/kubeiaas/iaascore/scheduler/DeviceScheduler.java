package kubeiaas.iaascore.scheduler;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Device;
import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.enums.device.DeviceStatusEnum;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.dao.feign.DeviceController;
import kubeiaas.iaascore.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Slf4j
@Configuration
public class DeviceScheduler {
    @Resource
    private DeviceController deviceController;

    @Resource
    private TableStorage tableStorage;

    public List<Device> queryAll(Host host) throws BaseException {
        // 1. Devices from host RAW
        List<Device> rawDevices;
        try {
            String jsonObjectString = deviceController.queryAll(getUri(host));
            rawDevices = JSON.parseArray(jsonObjectString, Device.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException("err: device query all failed, host name is " + host.getName());
        }

        // 2. Devices from DB
        List<Device> dbDevices = tableStorage.deviceQueryAllByHostUuid(host.getUuid());

        // 3. build total (device amount will not too large, this method O(n^2) is ok)
        List<Device> deviceList = new ArrayList<>();
        for (Device rawDev : rawDevices) {
            boolean addFlag = false;
            for (Device dbDev : dbDevices) {
                if (rawDev.equals(dbDev)) {
                    deviceList.add(dbDev);
                    dbDevices.remove(dbDev);
                    addFlag = true;
                    break;
                }
            }
            if (!addFlag) deviceList.add(rawDev);
        }

        for (Device dbDev : dbDevices) {
            dbDev.setStatus(DeviceStatusEnum.UNREACHABLE);
        }
        deviceList.addAll(dbDevices);

        return deviceList;
    }

    public List<Device> queryByVm(Vm vm) throws BaseException {
        // 1. Devices from DB
        List<Device> dbDevices = tableStorage.deviceQueryByVmUuid(vm.getUuid());
        if (CollectionUtils.isEmpty(dbDevices)) {
            return new ArrayList<>();
        }

        // 2. Devices form host RAW
        Host host = tableStorage.hostQueryByUuid(vm.getHostUuid());
        List<Device> rawDevices;
        try {
            String jsonObjectString = deviceController.queryAll(getUri(host));
            rawDevices = JSON.parseArray(jsonObjectString, Device.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException("err: device query all failed, host name is " + host.getName());
        }

        // 3. build total (device amount will not too large, this method O(n^2) is ok)
        List<Device> deviceList = new ArrayList<>();
        for (Device dbDev : dbDevices) {
            boolean findFlag = false;
            for (Device rawDev : rawDevices) {
                if (rawDev.equals(dbDev)) {
                    deviceList.add(dbDev);
                    findFlag = true;
                    break;
                }
            }
            if (!findFlag) {
                dbDev.setStatus(DeviceStatusEnum.UNREACHABLE);
                deviceList.add(dbDev);
            }
        }

        return deviceList;
    }

    public boolean attachDevice(Device attachDevice, Host host, Vm vm) throws BaseException {
        List<Device> deviceList = this.queryAll(host);
        for (Device device : deviceList) {
            if (device.equals(attachDevice) && device.getStatus().equals(DeviceStatusEnum.AVAILABLE)) {
                // attach device
                if (deviceController.attach(JSON.toJSONString(device), JSON.toJSONString(vm), getUri(host))
                        .equals(ResponseMsgConstants.SUCCESS)) {
                    log.info("attachDevice == success");
                } else {
                    log.info("attachDevice == error");
                    return false;
                }
                device.setStatus(DeviceStatusEnum.ATTACHED);
                device.setInstanceUuid(vm.getUuid());
                tableStorage.deviceSave(device);
                return true;
            }
        }
        log.info("attach ==== error: device not found");
        return false;
    }

    private URI getUri(Host host) {
        try {
            return new URI(AgentConfig.getHostUri(host));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            log.error("build URI failed!");
            return null;
        }
    }

}
