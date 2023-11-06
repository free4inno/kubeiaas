package kubeiaas.dbproxy.table;

import kubeiaas.common.bean.Mount;

import javax.persistence.*;

@Entity
@Table(name = "mount")
public class MountTable extends Mount {
    public MountTable() {
        super();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public Integer getId() {
        return super.getId();
    }

    public void setId(Integer id) {
        super.setId(id);
    }

    @Column(name = "vm_uuid")
    public String getVmUuid() {
        return super.getVmUuid();
    }

    public void setVmUuid(String vmUuid) {
        super.setVmUuid(vmUuid);
    }

    @Column(name = "iso_uuid")
    public String getIsoUuid() {
        return super.getIsoUuid();
    }

    public void setIsoUuid(String isoUuid) {
        super.setIsoUuid(isoUuid);
    }

    @Column(name = "type")
    public String getType() {
        return super.getType();
    }

    public void setType(String type) {
        super.setType(type);
    }

    @Column(name = "device")
    public String getDevice() {
        return super.getDevice();
    }

    public void setDevice(String device) {
        super.setDevice(device);
    }

    @Column(name = "driver_type")
    public String getDriverType() {
        return super.getDriverType();
    }

    public void setDriverType(String driverType) {
        super.setDriverType(driverType);
    }

    @Column(name = "source")
    public String getSource() {
        return super.getSource();
    }

    public void setSource(String source) {
        super.setSource(source);
    }

    @Column(name = "mount_point")
    public String getMountPoint() {
        return super.getMountPoint();
    }

    public void setMountPoint(String mountPoint) {
        super.setMountPoint(mountPoint);
    }

    @Column(name = "bus")
    public String getBus() {
        return super.getBus();
    }

    public void setBus(String bus) {
        super.setBus(bus);
    }

    @Column(name = "readonly")
    public Boolean getReadonly() {
        return super.getReadonly();
    }

    public void setReadonly(Boolean readonly) {
        super.setReadonly(readonly);
    }
}
