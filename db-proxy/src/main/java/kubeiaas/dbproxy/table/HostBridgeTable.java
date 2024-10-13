package kubeiaas.dbproxy.table;

import kubeiaas.common.bean.HostBridge;

import javax.persistence.*;

@Entity
@Table(name = "host_bridge")
public class HostBridgeTable extends HostBridge {
    public HostBridgeTable() {
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

    @Column(name = "host_uuid")
    public String getHostUuid() {
        return super.getHostUuid();
    }

    public void setHostUuid(String hostUuid) {
        super.setHostUuid(hostUuid);
    }

    @Column(name = "bridge_name")
    public String getBridgeName() {
        return super.getBridgeName();
    }

    public void setBridgeName(String bridgeName) {
        super.setBridgeName(bridgeName);
    }

    // !NOT_IN_DB, 用于展示的主机名称，不存储在数据库中
//    @Transient
    @Column(name = "host_name")
    public String getHostName() {
        return super.getHostName();
    }

    public void setHostName(String hostName) {
        super.setHostName(hostName);
    }

    @Column(name = "ip_segment_id")
    public Integer getIpSegmentId() {
        return super.getIpSegmentId();
    }

    public void setIpSegmentId(Integer ipSegmentId) {
        super.setIpSegmentId(ipSegmentId);
    }
}
