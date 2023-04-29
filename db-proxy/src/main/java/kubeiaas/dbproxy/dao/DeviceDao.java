package kubeiaas.dbproxy.dao;

import kubeiaas.dbproxy.table.DeviceTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DeviceDao extends JpaRepository<DeviceTable, Integer>, JpaSpecificationExecutor<DeviceTable> {

}
