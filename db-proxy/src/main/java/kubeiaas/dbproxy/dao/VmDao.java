package kubeiaas.dbproxy.dao;

import kubeiaas.dbproxy.table.VmTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.transaction.Transactional;

public interface VmDao extends JpaRepository<VmTable, Integer>, JpaSpecificationExecutor<VmTable> {

    @Transactional
    void deleteByUuid(String uuid);
}
