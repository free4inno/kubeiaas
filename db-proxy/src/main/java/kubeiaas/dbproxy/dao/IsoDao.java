package kubeiaas.dbproxy.dao;

import kubeiaas.dbproxy.table.IsoTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.transaction.Transactional;

public interface IsoDao extends JpaRepository<IsoTable, Integer>, JpaSpecificationExecutor<IsoTable> {
    @Transactional
    void deleteByUuid(String uuid);

    IsoTable findByUuid(String uuid);
}
