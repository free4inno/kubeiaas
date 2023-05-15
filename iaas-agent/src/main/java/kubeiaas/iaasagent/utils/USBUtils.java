package kubeiaas.iaasagent.utils;

import kubeiaas.common.bean.Device;
import kubeiaas.common.enums.device.DeviceTypeEnum;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class USBUtils {

    public static List<Device> getHostDevices () {
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

                StringBuilder sb = new StringBuilder();

                // - BUS
                boolean zeroFlag = true;
                for (int i = 0; i < devInfo[1].length(); i++){
                    char ch = devInfo[1].charAt(i);
                    if (ch != '0' && zeroFlag) zeroFlag = false;
                    if (!zeroFlag) sb.append(ch);
                }
                String busStr = sb.toString();
                usbDevice.setBus(Integer.parseInt(busStr));

                // - DEV
                sb.setLength(0);
                zeroFlag = true;
                for (int i = 0; i < devInfo[3].length() - 1; i++){
                    char ch = devInfo[3].charAt(i);
                    if (ch != '0' && zeroFlag) zeroFlag = false;
                    if (!zeroFlag) sb.append(ch);
                }
                String devStr = sb.toString();
                usbDevice.setDev(Integer.parseInt(devStr));

                // - Vendor
                String vendorStr = "0x" + devInfo[5].split(":")[0];
                usbDevice.setVendor(Integer.decode(vendorStr));
                // - Product
                String productStr = "0x" + devInfo[5].split(":")[1];
                usbDevice.setProduct(Integer.decode(productStr));

                // - Name
                sb.setLength(0);
                for (int i = 6; i < devInfo.length; i++) {
                    sb.append(devInfo[i]).append(" ");
                }
                usbDevice.setName(sb.toString());

                // exclude root hub
                if (usbDevice.getDev() != 1) {
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
