package kubeiaas.dbproxy.table;

import kubeiaas.common.bean.Iso;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "iso")
public class IsoTable extends Iso {
    public IsoTable() {
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

    @Column(name = "size")
    public long getSize() {
        return super.getSize();
    }

    public void setSize(long size) {
        super.setSize(size);
    }

    @Column(name = "host_uuid")
    public String getHostUuid() {
        return super.getHostUuid();
    }

    public void setHostUuid(String hostUuid) {
        super.setHostUuid(hostUuid);
    }

    @Column(name = "relative_path")
    public String getRelativePath() {
        return super.getRelativePath();
    }

    public void setRelativePath(String relativePath) {
        super.setRelativePath(relativePath);
    }

    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return super.getCreateTime();
    }

    public void setCreateTime(Timestamp createTime) {
        super.setCreateTime(createTime);
    }
}
