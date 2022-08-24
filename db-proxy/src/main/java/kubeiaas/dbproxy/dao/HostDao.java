package kubeiaas.dbproxy.dao;

import kubeiaas.dbproxy.table.HostTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HostDao extends JpaRepository<HostTable, Integer>, JpaSpecificationExecutor<HostTable> {

}
