package kubeiaas.dbproxy.dao;

import kubeiaas.dbproxy.table.HostBridgeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HostBridgeDao extends JpaRepository<HostBridgeTable, Integer>, JpaSpecificationExecutor<HostBridgeTable> {

}
