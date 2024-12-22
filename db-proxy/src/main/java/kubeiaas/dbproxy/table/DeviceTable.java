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
    public Integer getBus() {
        return super.getBus();
    }

    @Override
    @Column(name = "dev")
    public Integer getDev() {
        return super.getDev();
    }

    @Override
    @Column(name = "vendor")
    public Integer getVendor() {
        return super.getVendor();
    }

    @Override
    @Column(name = "product")
    public Integer getProduct() {
        return super.getProduct();
    }

    @Override
    @Column(name = "domain")
    public Integer getDomain() {
        return super.getDomain();
    }

    @Override
    @Column(name = "slot")
    public Integer getSlot() {
        return super.getSlot();
    }

    @Override
    @Column(name = "function")
    public Integer getFunction() {
        return super.getFunction();
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
