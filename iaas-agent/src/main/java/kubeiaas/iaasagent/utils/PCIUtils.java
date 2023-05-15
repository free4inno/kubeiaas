package kubeiaas.iaasagent.utils;

import kubeiaas.common.bean.Device;
import kubeiaas.common.enums.device.DeviceTypeEnum;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PCIUtils {

    public static List<Device> getHostDevices () {
        List<Device> deviceList = new ArrayList<>();
        // shell call
        InputStream is = null;
        BufferedReader br = null;
        try {
            String[] cmd = {"/bin/sh", "-c", "lspci -nn -D | grep 'VGA\\|Audio' | grep NVIDIA"};
            Process proc = Runtime.getRuntime().exec(cmd);
            is = proc.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            // fill msg
            String lineStr;
            while ((lineStr = br.readLine()) != null) {
                Device pciDevice = new Device(DeviceTypeEnum.PCI);
                // example: 0000:01:00.0 VGA compatible controller [0300]: NVIDIA Corporation Device [10de:2684] (rev a1)
                String[] devInfo = lineStr.split(" ");
                String[] signs = devInfo[0].split(":");

                // - DOMAIN
                String domain = signs[0];
                pciDevice.setDomain(numberStrToInteger(domain));

                // - BUS
                String bus = signs[1];
                pciDevice.setBus(numberStrToInteger(bus));

                // - SLOT
                String slot = signs[2].split("\\.")[0];
                pciDevice.setSlot(numberStrToInteger(slot));

                // - Function
                String func = signs[2].split("\\.")[1];
                pciDevice.setFunction(numberStrToInteger(func));

                // - Name
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < devInfo.length; i++) {
                    sb.append(devInfo[i]).append(" ");
                }
                pciDevice.setName(sb.toString());

                deviceList.add(pciDevice);
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

    private static Integer numberStrToInteger(String num) {
        StringBuilder sb = new StringBuilder();
        boolean notZeroFlag = false;
        for (int i = 0; i < num.length(); i++) {
            char ch = num.charAt(i);
            if (ch != '0') {
                notZeroFlag = true;
            }
            if (notZeroFlag) {
                sb.append(ch);
            }
        }
        if (sb.length() == 0) {
            return 0;
        }
        return Integer.parseInt(sb.toString());
    }

}
