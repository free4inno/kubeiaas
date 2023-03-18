package kubeiaas.iaasagent.utils;

import kubeiaas.common.bean.Device;
import kubeiaas.common.enums.device.DeviceTypeEnum;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class UsbUtils {

    public static List<Device> getUsbDevice () {
        List<Device> deviceList = new ArrayList<>();
        // shell call
        InputStream is = null;
        BufferedReader br = null;
        try {
            String[] cmd = {"/bin/sh", "-c", "lsusb"};
            Process proc = Runtime.getRuntime().exec(cmd);
            is = proc.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            // fill msg
            String lineStr;
            while ((lineStr = br.readLine()) != null) {
                Device usbDevice = new Device(DeviceTypeEnum.USB);
                // example: Bus 002 Device 001: ID 1d6b:0003 Linux Foundation 3.0 root hub
                String[] devInfo = lineStr.split(" ");

                usbDevice.setBus(devInfo[1]);
                usbDevice.setDev(devInfo[3].split(":")[0]);
                usbDevice.setVendor("0x" + devInfo[5].split(":")[0]);
                usbDevice.setProduct("0x" + devInfo[5].split(":")[1]);

                StringBuilder sb = new StringBuilder();
                for (int i = 6; i < devInfo.length; i++) {
                    sb.append(devInfo[i]).append(" ");
                }
                usbDevice.setName(sb.toString());

                // exclude root hub
                if (Integer.parseInt(usbDevice.getDev()) != 1) {
                    deviceList.add(usbDevice);
                }
            }
            proc.destroy();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // close BufferedReader
        if (br != null) {
            try {
                br.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // close InputStream
        if (is != null) {
            try {
                is.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return deviceList;
    }

}
