package kubeiaas.dbproxy.dao;

import kubeiaas.dbproxy.table.IpUsedTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IpUsedDao extends JpaRepository<IpUsedTable, Integer>, JpaSpecificationExecutor<IpUsedTable> {

}
