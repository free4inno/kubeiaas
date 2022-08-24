package kubeiaas.dbproxy.table;

import kubeiaas.common.bean.Vm;
import kubeiaas.common.enums.vm.VmStatusEnum;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "vm")
public class VmTable extends Vm {

    public VmTable() {
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

    @Column(name = "password")
    public String getPassword() {
        return super.getPassword();
    }

    public void setPassword(String password) {
        super.setPassword(password);
    }

    @Column(name = "cpus")
    public int getCpus() {
        return super.getCpus();
    }

    public void setCpus(int cpus) {
        super.setCpus(cpus);
    }

    @Column(name = "memory")
    public int getMemory() {
        return super.getMemory();
    }

    public void setMemory(int memory) {
        super.setMemory(memory);
    }

    @Column(name = "disk_size")
    public int getDiskSize() {
        return super.getDiskSize();
    }

    public void setDiskSize(int diskSize) {
        super.setDiskSize(diskSize);
    }

    @Column(name = "vnc_port")
    public String getVncPort() {
        return super.getVncPort();
    }

    public void setVncPort(String vncPort) {
        super.setVncPort(vncPort);
    }

    @Column(name = "vnc_password")
    public String getVncPassword() {
        return super.getVncPassword();
    }

    public void setVncPassword(String vncPassword) {
        super.setVncPassword(vncPassword);
    }

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    public VmStatusEnum getStatus() {
        return super.getStatus();
    }

    public void setStatus(VmStatusEnum status) {
        super.setStatus(status);
    }

    @Column(name = "image_uuid")
    public String getImageUuid() {
        return super.getImageUuid();
    }

    public void setImageUuid(String imageUuid) {
        super.setImageUuid(imageUuid);
    }

    @Column(name = "host_uuid")
    public String getHostUuid() {
        return super.getHostUuid();
    }

    public void setHostUuid(String hostUuid) {
        super.setHostUuid(hostUuid);
    }

    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return super.getCreateTime();
    }

    public void setCreateTime(Timestamp createTime) {
        super.setCreateTime(createTime);
    }

}
