package kubeiaas.iaasagent.config;

import kubeiaas.common.bean.Image;
import kubeiaas.common.bean.IpUsed;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.image.ImageOSTypeEnum;
import kubeiaas.common.enums.network.IpTypeEnum;
import kubeiaas.common.enums.volume.VolumeFormatEnum;
import kubeiaas.common.utils.ShellUtils;
import kubeiaas.common.utils.VmCUtils;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 此类中操作xml文件的类为dom4j，可以很好地实现对xml文件构造
 * 生成虚拟机对应的xml文件.
 */
@Slf4j
@Configuration
public class LibvirtConfig {

    private static final String MAX_MEMORY = "16";
    private static final String MAX_CPU = "16";

    public static String emulatorName = "/usr/bin/qemu-system-x86_64";    //the location of kvm simulation
    public static String virConStr = "qemu:///system";

    public static String networkType = System.getenv("NETWORK_BRIDGE_TYPE");
    public static final String NETWORK_TYPE_LINUX = "Linux";
    public static final String NETWORK_TYPE_OVS = "OVS";
    public static final String NETWORK_TYPE_MACV = "MACVLAN";

    public static String getVncPort = "virsh vncdisplay ";

    /**
     * old：使用构造函数在启动时调用
     * now：在kvm操作前主动调用
     * （为了启动时不依赖宿主机的 libvirt 安装，暂时不考虑额外造成的开销）
     */
    public void initEmulator() {
        emulatorName = getEmulatorLocation();
    }

    /**
     * 获取 xml 中 emulator 的位置，目前 CentOS 是 /usr/local/bin/qemu-system-x86_64, Ubuntu 是 /usr/bin/qemu-system-x86_64
     * 为了防止之后 emulator 路径会改变，或者运维时设置错误，目前使用三种方式可以更改：
     *
     * 1、使用 whereis 命令，查找全部 emulator 的路径，找到中间含有 usr & bin 的路径，然后返回这个值;
     * 2、默认使用查找到的首个位置
     * 3、加载 static 中的 emulatorName 的默认值。
     *
     * 三个方法有先后顺序，一个生效之后，则不执行剩下的步骤，所以更改时，一定注意先后顺序。
     */
    private static String getEmulatorLocation() {
        log.info("execute \"whereis qemu-system-x86_64\" cmd ");
        String tempLocation = ShellUtils.getCmd("whereis qemu-system-x86_64");
        /* CentOS:
         *      qemu-system-x86_64: /usr/local/bin/qemu-system-x86_64
         * Ubuntu:
         *      qemu-system-x86_64: /usr/bin/qemu-system-x86_64
         */
        String[] emulators = tempLocation.split(" ");
        for (String emulator : emulators) {
            if (emulator.contains("usr") && emulator.contains("bin")) {
                return emulator;
            }
        }
        int num = 2;
        if (num < emulators.length) {
            return emulators[num];
        }
        return emulatorName;
    }

    /**
     * 生成xml描述文件.
     * 参数解析、流程控制.
     */
    public String generateDesXML(
            Vm instance,
            Image image,
            List<Volume> volumes,
            List<IpUsed> ips,
            int cpu,
            int memory) {
        log.info("generateDesXML ---- start ----");

        Document document = DocumentHelper.createDocument();
        Element domain = document.addElement("domain").addAttribute("type", "kvm")
                .addNamespace("qemu", "http://libvirt.org/schemas/domain/qemu/1.0");
        String vmName = VmCUtils.generateName(instance.getUuid(), "");
        domain.addElement("name")
                .setText(vmName);
        domain.addElement("uuid")
                .setText(instance.getUuid());
        domain.addElement("memory")
                .setText(String.valueOf(VmCUtils.memUnitConvert(Integer.valueOf(MAX_MEMORY))));
        domain.addElement("currentMemory")
                .setText(String.valueOf(VmCUtils.memUnitConvert(memory)));
        domain.addElement("vcpu")
                .addAttribute("placement", "static")
                .addAttribute("current", String.valueOf(cpu))
                .setText(MAX_CPU);
        domain.addElement("cpu")
                .addElement("topology")
                .addAttribute("sockets", "1")
                .addAttribute("cores", MAX_CPU)
                .addAttribute("threads", "1");

        Element os = domain.addElement("os");
        os.addElement("type").setText(image.getOsMode().toString()); //需验证参数是否为空
        os.addElement("bootmenu").addAttribute("enable", "yes");        //为了兼容windows，不能指定boot dev

        //一些确定值
        Element features = domain.addElement("features");
        features.addElement("acpi");
        features.addElement("apic");
        if(image.getOsType().equals(ImageOSTypeEnum.WINDOWS)){
            Element hyperv = features.addElement("hyperv");
            hyperv.addElement("relaxed").addAttribute("state", "on");
            hyperv.addElement("vapic").addAttribute("state", "on");
            hyperv.addElement("spinlocks").addAttribute("state", "on").addAttribute("retries", "8191");
        }else{
            features.addElement("pae");
        }
        Element clock = domain.addElement("clock").addAttribute("offset", "localtime");
        clock.addElement("timer").addAttribute("name", "pit").addAttribute("tickpolicy", "delay");
        clock.addElement("timer").addAttribute("name", "rtc").addAttribute("tickpolicy", "catchup");
        if(image.getOsType().equals(ImageOSTypeEnum.WINDOWS)){
            clock.addElement("timer").addAttribute("name", "hpet").addAttribute("present", "no");
            clock.addElement("timer").addAttribute("name", "hypervclock").addAttribute("present", "yes");
        }
        domain.addElement("on_poweroff").setText("destroy");
        domain.addElement("on_reboot").setText("restart");
        domain.addElement("on_crash").setText("restart");

        //volume设置
        Element devices = domain.addElement("devices");
        initEmulator();  // get emulator path
        if (emulatorName != null) {
            devices.addElement("emulator").setText(emulatorName);          //通过conf类加载，默认为 kvm 在Linux中的位置，可以在properties中更改
        }

        for (Volume volume : volumes) {
            volumeToDiskXml(volume, devices);
        }

        //网络设置
        for (IpUsed ip : ips) {
            ipToNetworkXml(ip, devices);
        }

        //Tab设置
        devices.addElement("input").addAttribute("type", "tablet")
                .addAttribute("bus", "usb");

        //VNC设置
        Element graphicsVNC = devices.addElement("graphics")
                .addAttribute("type", "vnc")
                .addAttribute("port", "-1")
                .addAttribute("autoport", "yes")
                .addAttribute("listen", "0.0.0.0")
                .addAttribute("passwd", VmCUtils.getVNCPasswd(instance.getId(), instance.getUuid()));
        graphicsVNC.addElement("listen")
                .addAttribute("type", "address")
                .addAttribute("address", "0.0.0.0");

        log.info("generateDesXML ---- end ----");
        return document.asXML();
    }

    public String ipToNetwork(IpUsed ip) {
        log.info("ipToNetwork ---- start ----");
        Document root = DocumentHelper.createDocument();
        Element devices = root.addElement("root");
        ipToNetworkXml(ip, devices);
        String res = devices.asXML();
        res = res.replaceAll("<root>", "");
        res = res.replaceAll("</root>", "");
        log.info("ipToNetwork ---- end ---- res: " + res.trim());
        return res.trim();
    }

    private void ipToNetworkXml(IpUsed ip, Element devices) {
        log.info("ipToNetworkXml ---- start ----");
        Element netInterface = devices.addElement("interface");

        switch (networkType) {
            case NETWORK_TYPE_LINUX:
                // == Linux: target & device
                log.info("---- NETWORK_TYPE: Linux (tap={})", getTap(ip.getMac()));

                netInterface.addAttribute("type", "bridge");
                // 设置基本网卡信息
                netInterface.addElement("mac")
                        .addAttribute("address", ip.getMac());
                // 设置所用网桥
                netInterface.addElement("source")
                        .addAttribute("bridge", ip.getBridge());
                // set target
                netInterface.addElement("target").
                        addAttribute("dev", getTap(ip.getMac()));
                break;

            case NETWORK_TYPE_OVS:
                // == OVS: virtualPort & type(OpenVSwitch)
                log.info("---- NETWORK_TYPE: OVS");

                netInterface.addAttribute("type", "bridge");
                // 设置基本网卡信息
                netInterface.addElement("mac")
                        .addAttribute("address", ip.getMac());
                // 设置所用网桥
                netInterface.addElement("source")
                        .addAttribute("bridge", ip.getBridge());
                // set virtualport
                netInterface.addElement("virtualport").
                        addAttribute("type", "openvswitch");
                break;

            case NETWORK_TYPE_MACV:
            default:
                // == MACV: macvlan & macvtap
                log.info("---- NETWORK_TYPE: MACV");

                netInterface.addAttribute("type", "direct");
                // 设置基本网卡信息
                netInterface.addElement("mac")
                        .addAttribute("address", ip.getMac());
                // 设置直连设备
                netInterface.addElement("source")
                        .addAttribute("dev", ip.getBridge()).addAttribute("mode", "bridge");
                // set target
                netInterface.addElement("target").
                        addAttribute("dev", getTap(ip.getMac()));
                break;
        }

        // 设置网卡模式类型为 Virtio
        netInterface.addElement("model").
                addAttribute("type", "virtio");

        log.info("ipToNetworkXml ---- end ----");
    }

    //目前dom4j没有办法获取部分xml内容，只能使用这种笨方法
    public String volumeToDisk(Volume volume) {
        log.info("volumeToDisk ---- start ----");
        Document root = DocumentHelper.createDocument();
        Element devices = root.addElement("root");
        volumeToDiskXml(volume, devices);
        String res = devices.asXML();
        res = res.replaceAll("<root>", "");
        res = res.replaceAll("</root>", "");
        log.info("volumeToDisk ---- end ---- res: " + res.trim());
        return res.trim();
    }

    private void volumeToDiskXml(Volume volume, Element devices) {
        log.info("volumeToDiskXml ---- start ----");
        String volumeType = getVolumeDiskDevice(volume.getFormatType());

        Element disk = devices.addElement("disk")
                .addAttribute("type", "file")
                .addAttribute("device", volumeType);   //disk, cdrom
        //cdrom, driver type --- raw
        if (volumeType.equals(VolumeConstants.VOLUME_DEVICE_CDROM)) {
            disk.addElement("driver")
                    .addAttribute("name", "qemu")
                    .addAttribute("type", "raw");
        } else {
            disk.addElement("driver")
                    .addAttribute("name", "qemu")
                    .addAttribute("type", "qcow2");
        }

        disk.addElement("source").addAttribute("file", VolumeConstants.DEFAULT_NFS_SRV_PATH + volume.getProviderLocation());
        disk.addElement("target")
                .addAttribute("dev", volume.getMountPoint())
                .addAttribute("bus", volume.getBus());

        log.info("volumeToDiskXml ---- end ----");
    }

    private String getVolumeDiskDevice(VolumeFormatEnum volumeType) {
        if (volumeType.equals(VolumeFormatEnum.ISO)) {
            return VolumeConstants.VOLUME_DEVICE_CDROM;
        }
        return VolumeConstants.VOLUME_DEVICE_DISK;
    }

    private String getTap(String str) {
        str = str.replaceAll(":", "");
        return VolumeConstants.TAP_PREFIX + str;
    }

}
