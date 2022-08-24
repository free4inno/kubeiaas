package kubeiaas.dbproxy.table;

import kubeiaas.common.bean.IpUsed;
import kubeiaas.common.enums.network.IpAttachEnum;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "ip_used")
public class IpUsedTable extends IpUsed {

    public IpUsedTable() {
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

    @Column(name = "ip")
    public String getIp() {
        return super.getIp();
    }

    public void setIp(String ip) {
        super.setIp(ip);
    }

    @Column(name = "mac")
    public String getMac() {
        return super.getMac();
    }

    public void setMac(String mac) {
        super.setMac(mac);
    }

    @Column(name = "ip_segment_id")
    public int getIpSegmentId() {
        return super.getIpSegmentId();
    }

    public void setIpSegmentId(int ipSegmentId) {
        super.setIpSegmentId(ipSegmentId);
    }

    @Column(name = "instance_uuid")
    public String getInstanceUuid() {
        return super.getInstanceUuid();
    }

    public void setInstanceUuid(String instanceUuid) {
        super.setInstanceUuid(instanceUuid);
    }

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    public IpAttachEnum getStatus() {
        return super.getStatus();
    }

    public void setStatus(IpAttachEnum status) {
        super.setStatus(status);
    }

    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return super.getCreateTime();
    }

    public void setCreateTime(Timestamp createTime) {
        super.setCreateTime(createTime);
    }
}
