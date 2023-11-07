package kubeiaas.common.bean;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mount {
    private Integer id; //自增主键
    private String vmUuid; // 虚拟机的UUID
    private String isoUuid; // ISO的UUID
    /* ********** Disk ********** */
    private String type; // Disk backend storage type ('file', 'block', 'dir', 'network', etc.)
    private String device; // Type of device ('disk', 'cdrom', 'floppy', 'lun')
    /* ********** Driver ********** */
    private String driverType; // Type of driver ('raw', 'qcow2', etc.)
    /* ********** Source ********** */
    private String source; // Path to the backend storage (relevant for 'file' type)
    /* ********** Target device ********** */
    private String mountPoint; // Target device name ('vda', 'hda', etc.)
    private String bus; // bus，例如：sata0, ide1 等
    /* ********** Others ********** */
    private Boolean readonly; // Whether the device is read-only
}
