package kubeiaas.dbproxy.table;

import kubeiaas.common.bean.Host;
import kubeiaas.common.enums.host.HostStatusEnum;

import javax.persistence.*;

@Entity
@Table(name = "host")
public class HostTable extends Host {

    public HostTable() {
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

    @Column(name = "ip")
    public String getIp() {
        return super.getIp();
    }

    public void setIp(String ip) {
        super.setIp(ip);
    }

    @Column(name = "config")
    public String getConfig() {
        return super.getConfig();
    }

    public void setConfig(String config) {
        super.setConfig(config);
    }

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    public HostStatusEnum getStatus() {
        return super.getStatus();
    }

    public void setStatus(HostStatusEnum status) {
        super.setStatus(status);
    }

    @Column(name = "role")
    public String getRole() {
        return super.getRole();
    }

    public void setRole(String role) {
        super.setRole(role);
    }

}
