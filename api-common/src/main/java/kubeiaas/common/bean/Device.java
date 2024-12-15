package kubeiaas.common.bean;

import kubeiaas.common.enums.device.DeviceStatusEnum;
import kubeiaas.common.enums.device.DeviceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    private Integer id;
    private DeviceTypeEnum type;

    private Integer bus;
    private Integer dev;
    private Integer domain;
    private Integer slot;
    private Integer function;
    private Integer vendor;
    private Integer product;

//    private String uuid;

    /**
     * Libvirt USB device:
     *
     *   <source>
     *     <vendor id=''/>
     *     <product id=''/>
     *     <address bus='' device=''/>
     *   </source>
     *
     * Libvirt PCI device:
     *
     *   <source>
     *     <address domain='' bus='' slot='' function=''/>
     *   </source>
     *
     * Libvirt MDEV device:
     *  hypervisor with mdev VFIO framework:
     *   <hostdev mode='subsystem' type='mdev' managed='yes'>
     *       <source>
     *           <address uuid='0a2b52d7-587f-4acd-9e7c-faf1d6751630'/>
     *       </source>
     *   </hostdev>
     *
     *  hypervisor with vendor-specific VFIO framework:
     *   <hostdev mode='subsystem' type='pci' managed='no'>
     *       <source>
     *           <address domain='domain' bus='bus' slot='vf-slot' function='v-function'/>
     *       </source>
     *   </hostdev>
     *
     */

    private String hostUuid;
    private String name;

    private DeviceStatusEnum status;
    private String instanceUuid;

    // --------- NOT IN DB ---------
    private String sign;

    // -----------------------------

    /**
     * construct a Device with type
     */
    public Device(DeviceTypeEnum type) {
        this.setType(type);
    }

    /**
     * construct temp Device for compare
     */
    public Device(DeviceTypeEnum type, String sign) throws Exception {
        this.setType(type);
        Integer[] signs = decodeSign(sign);
        /**
         * USB DEVICE:
         *      bus:dev:vendor:product
         * PCI DEVICE:
         *      domain:bus:slot:function
         */
        switch (type) {
            case USB:
                this.setBus(signs[0]);
                this.setDev(signs[1]);
                this.setVendor(signs[2]);
                this.setProduct(signs[3]);
                break;
            case PCI:
                this.setDomain(signs[0]);
                this.setBus(signs[1]);
                this.setSlot(signs[2]);
                this.setFunction(signs[3]);
                break;
            case MDEV:
                this.setDomain(signs[0]);
                this.setBus(signs[1]);
                this.setSlot(signs[2]);
                this.setFunction(signs[3]);
                break;
        }
    }

    /**
     * equals for compare
     */
    public boolean equals(Device device) {
        if (null == device)
            return false;
        if (null == type || null == device.getType() || !type.equals(device.getType()))
            return false;

        String thisSign = this.encodeSign();
        String thatSign = device.encodeSign();

        return thisSign.equals(thatSign);
    }

    /**
     * A sign to identity union device.
     *
     * USB DEVICE:
     *      bus:dev:vendor:product
     * PCI DEVICE:
     *      domain:bus:slot:function
     */
    public String encodeSign() {
        StringBuilder sb = new StringBuilder();
//        if (type == null) {
//            type = DeviceTypeEnum.MDEV;
//        }
        switch (type) {
            case USB:
                sb.append(Integer.toHexString(bus))
                        .append(":").append(Integer.toHexString(dev))
                        .append(":").append(Integer.toHexString(vendor))
                        .append(":").append(Integer.toHexString(product));
                break;
            case PCI:
            case MDEV:
                sb.append(Integer.toHexString(domain))
                        .append(":").append(Integer.toHexString(bus))
                        .append(":").append(Integer.toHexString(slot))
                        .append(":").append(Integer.toHexString(function));
                break;
        }
        return sb.toString();
    }

    private Integer[] decodeSign(String sign) throws Exception {
        String[] signs = sign.split(":");
        if (signs.length != 4) {
            throw new Exception("Device decode error: length not 4.");
        }
        Integer[] intSigns = new Integer[4];
        for (int i = 0; i < 4; i++) {
//            if (signs[i].contains("0x")) {
//                intSigns[i] = Integer.decode(signs[i]);
//            } else {
//                intSigns[i] = Integer.parseInt(signs[i]);
//            }
            intSigns[i] = Integer.parseInt(signs[i], 16);
        }
        return intSigns;
    }
}
