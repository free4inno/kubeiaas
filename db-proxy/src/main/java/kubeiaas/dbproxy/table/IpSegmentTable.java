package kubeiaas.dbproxy.table;

import kubeiaas.common.bean.IpSegment;
import kubeiaas.common.enums.network.IpTypeEnum;

import javax.persistence.*;

@Entity
@Table(name = "ip_segment")
public class IpSegmentTable extends IpSegment {
    public IpSegmentTable() {
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

    @Column(name = "name")
    public String getName() {
        return super.getName();
    }

    public void setName(String name) {
        super.setName(name);
    }

    @Column(name = "netmask")
    public String getNetmask() {
        return super.getNetmask();
    }

    public void setNetmask(String netmask) {
        super.setNetmask(netmask);
    }

    @Column(name = "gateway")
    public String getGateway() {
        return super.getGateway();
    }

    public void setGateway(String gateway) {
        super.setGateway(gateway);
    }

    @Column(name = "dns")
    public String getDns() {
        return super.getDns();
    }

    public void setDns(String dns) {
        super.setDns(dns);
    }

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    public IpTypeEnum getType() {
        return super.getType();
    }

    public void setType(IpTypeEnum type) {
        super.setType(type);
    }

    @Column(name = "ip_range_start")
    public String getIpRangeStart() {
        return super.getIpRangeStart();
    }

    public void setIpRangeStart(String startIp) {
        super.setIpRangeStart(startIp);
    }

    @Column(name = "ip_range_end")
    public String getIpRangeEnd() {
        return super.getIpRangeEnd();
    }

    public void setIpRangeEnd(String endIp) {
        super.setIpRangeEnd(endIp);
    }

    @Column(name = "host_uuid")
    public String getHostUuid() {
        return super.getHostUuid();
    }

    public void setHostUuid(String hostUuid) {
        super.setHostUuid(hostUuid);
    }

    @Column(name = "bridge")
    public String getBridge() {
        return super.getBridge();
    }

    public void setBridge(String bridge) {
        super.setBridge(bridge);
    }
}
