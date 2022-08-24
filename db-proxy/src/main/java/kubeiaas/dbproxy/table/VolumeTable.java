package kubeiaas.dbproxy.table;

import kubeiaas.common.bean.Volume;
import kubeiaas.common.enums.volume.VolumeFormatEnum;
import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.common.enums.volume.VolumeUsageEnum;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "volume")
public class VolumeTable extends Volume {
    public VolumeTable() {
        super();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public int getId() {
        return super.getId();
    }

    public void setId(int id) {
        super.setId(id);
    }

    @Column(name = "uuid")
    public String getUuid() {
        return super.getUuid();
    }

    public void setUuid(String uuid) {
        super.setUuid(uuid);
    }

    @Column(name = "name")
    public String getName() {
        return super.getName();
    }

    public void setName(String name) {
        super.setName(name);
    }

    @Column(name = "description")
    public String getDescription() {
        return super.getDescription();
    }

    public void setDescription(String description) {
        super.setDescription(description);
    }

    @Column(name = "host_uuid")
    public String getHostUuid() {
        return super.getHostUuid();
    }

    public void setHostUuid(String hostUuid) {
        super.setHostUuid(hostUuid);
    }

    @Column(name = "image_uuid")
    public String getImageUuid() {
        return super.getImageUuid();
    }

    public void setImageUuid(String uuid) {
        super.setImageUuid(uuid);
    }

    @Column(name = "size")
    public int getSize() {
        return super.getSize();
    }

    public void setSize(int size) {
        super.setSize(size);
    }

    @Column(name = "provider_location")
    public String getProviderLocation() {
        return super.getProviderLocation();
    }

    public void setProviderLocation(String providerLocation) {
        super.setProviderLocation(providerLocation);
    }

    @Column(name = "format_type")
    @Enumerated(EnumType.STRING)
    public VolumeFormatEnum getFormatType() {
        return super.getFormatType();
    }

    public void setFormatType(VolumeFormatEnum volumeType) {
        super.setFormatType(volumeType);
    }

    @Column(name = "usage_type")
    @Enumerated(EnumType.STRING)
    public VolumeUsageEnum getUsageType() {
        return super.getUsageType();
    }

    public void setUsageType(VolumeUsageEnum usageType) {
        super.setUsageType(usageType);
    }

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    public VolumeStatusEnum getStatus() {
        return super.getStatus();
    }

    public void setStatus(VolumeStatusEnum status) {
        super.setStatus(status);
    }

    @Column(name = "instance_uuid")
    public String getInstanceUuid() {
        return super.getInstanceUuid();
    }

    public void setInstanceUuid(String uuid) {
        super.setInstanceUuid(uuid);
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

    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return super.getCreateTime();
    }

    public void setCreateTime(Timestamp createTime) {
        super.setCreateTime(createTime);
    }
}
