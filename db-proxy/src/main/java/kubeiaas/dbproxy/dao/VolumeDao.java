package kubeiaas.dbproxy.dao;

import kubeiaas.dbproxy.table.VolumeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.transaction.Transactional;

public interface VolumeDao extends JpaRepository<VolumeTable, Integer>, JpaSpecificationExecutor<VolumeTable> {

    @Transactional
    void deleteByUuid(String uuid);
}
