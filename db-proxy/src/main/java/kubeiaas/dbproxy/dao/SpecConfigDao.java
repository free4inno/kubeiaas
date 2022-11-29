package kubeiaas.dbproxy.dao;

import kubeiaas.dbproxy.table.SpecConfigTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpecConfigDao extends JpaRepository<SpecConfigTable, Integer>, JpaSpecificationExecutor<SpecConfigTable> {

}
