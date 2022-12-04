package kubeiaas.dbproxy.table;

import kubeiaas.common.bean.SpecConfig;
import kubeiaas.common.enums.config.SpecTypeEnum;

import javax.persistence.*;

@Entity
@Table(name = "spec_config")
public class SpecConfigTable extends SpecConfig {
    public SpecConfigTable() {
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

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    public SpecTypeEnum getType() {
        return super.getType();
    }

    public void setType(SpecTypeEnum type) {
        super.setType(type);
    }

    @Column(name = "value")
    public String getValue() {
        return super.getValue();
    }

    public void setValue(String value) {
        super.setValue(value);
    }

    @Column(name = "description")
    public String getDescription() {
        return super.getDescription();
    }

    public void setDescription(String description) {
        super.setDescription(description);
    }

}
