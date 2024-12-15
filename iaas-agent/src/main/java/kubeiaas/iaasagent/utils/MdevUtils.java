package kubeiaas.iaasagent.utils;

import kubeiaas.common.bean.Device;
import kubeiaas.common.enums.device.DeviceTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MdevUtils {
    public static List<Device> getHostDevices() {
        List<Device> deviceList = new ArrayList<>();
        InputStream is = null;
        BufferedReader br = null;

        try {
            String[] cmd = {"mdevctl", "list"};
            Process proc = Runtime.getRuntime().exec(cmd);
            is = proc.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String lineStr;
            while ((lineStr = br.readLine()) != null) {
                try {
                    Device mdevDevice = parseVgpuFromCommandOutput(lineStr);
                    deviceList.add(mdevDevice);
                } catch (NumberFormatException e) {
                    log.error("ERROR: parse pci address failed, lineStr: " + lineStr);
                }
            }
        } catch (IOException e) {
            log.error("ERROR: getHostDevices failed");
            e.printStackTrace();
        }

        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                log.error("ERROR: BufferedReader close failed");
                e.printStackTrace();
            }
        }
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                log.error("ERROR: InputStream close failed");
                e.printStackTrace();
            }
        }

        return deviceList;
    }

    static Device parseVgpuFromCommandOutput(String lineStr) throws NumberFormatException {
//        line example: 0a2b52d7-587f-4acd-9e7c-faf1d6751630 0000:31:00.5 nvidia-562
//        uuid pci_id mdev_type
        Device mdevDevice = new Device(DeviceTypeEnum.MDEV);
        String[] devInfo = lineStr.split(" ");

        StringBuilder sb = new StringBuilder();
        // - UUID 作为 name 存储
        mdevDevice.setName(devInfo[0]);

        if (devInfo.length != 3) {
            log.error("ERROR: parseVgpuFromCommandOutput failed, invalid lineStr: " + lineStr);
            return mdevDevice;
        }

//        domain:bus:slot:function
        String[] pciInfo = devInfo[1].split(":");
        // - DOMAIN
        mdevDevice.setDomain(Integer.parseInt(pciInfo[0], 16));
        // - BUS
        mdevDevice.setBus(Integer.parseInt(pciInfo[1], 16));
        // - SLOT
        String[] slotFunc = pciInfo[2].split("\\.");
        if (slotFunc.length != 2) {
            log.error("ERROR: parseVgpuFromCommandOutput failed, invalid pciInfo: " + pciInfo[2]);
            return mdevDevice;
        }
        mdevDevice.setSlot(Integer.parseInt(slotFunc[0], 16));
        // - Function
        mdevDevice.setFunction(Integer.parseInt(slotFunc[1], 16));

        return mdevDevice;
    }
}
