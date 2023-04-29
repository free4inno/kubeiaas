package kubeiaas.dbproxy.table;

import kubeiaas.common.bean.Device;
import kubeiaas.common.enums.device.DeviceStatusEnum;
import kubeiaas.common.enums.device.DeviceTypeEnum;

import javax.persistence.*;

@Entity
@Table(name = "device")
public class DeviceTable extends Device {

    public DeviceTable() {
        super();
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public Integer getId() {
        return super.getId();
    }

    @Override
    @Column(name = "name")
    public String getName() {
        return super.getName();
    }

    @Override
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    public DeviceStatusEnum getStatus() {
        return super.getStatus();
    }

    @Override
    @Column(name = "bus")
    public String getBus() {
        return super.getBus();
    }

    @Override
    @Column(name = "dev")
    public String getDev() {
        return super.getDev();
    }

    @Override
    @Column(name = "vendor")
    public String getVendor() {
        return super.getVendor();
    }

    @Override
    @Column(name = "product")
    public String getProduct() {
        return super.getProduct();
    }

    @Override
    @Column(name = "host_uuid")
    public String getHostUuid() {
        return super.getHostUuid();
    }

    @Override
    @Column(name = "instance_uuid")
    public String getInstanceUuid() {
        return super.getInstanceUuid();
    }

    @Override
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    public DeviceTypeEnum getType() {
        return super.getType();
    }
}
