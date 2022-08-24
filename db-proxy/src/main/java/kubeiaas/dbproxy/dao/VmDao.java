package kubeiaas.dbproxy.dao;

import kubeiaas.dbproxy.table.VmTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VmDao extends JpaRepository<VmTable, Integer>, JpaSpecificationExecutor<VmTable> {

}
