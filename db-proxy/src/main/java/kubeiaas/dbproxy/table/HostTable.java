package kubeiaas.dbproxy.table;

import kubeiaas.common.bean.Host;

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
}
