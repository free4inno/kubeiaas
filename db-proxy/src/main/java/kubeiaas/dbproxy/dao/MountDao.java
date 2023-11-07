package kubeiaas.dbproxy.dao;

import kubeiaas.dbproxy.table.MountTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.List;

public interface MountDao extends JpaRepository<MountTable, Integer>, JpaSpecificationExecutor<MountTable> {
    @Transactional
    void deleteById(@NotNull Integer id);

    @Query("SELECT m.mountPoint FROM MountTable m WHERE m.vmUuid = :vmUuidStr")
    List<String> findMountPointsByVmUuid(@Param("vmUuidStr") String vmUuidStr);

    List<MountTable> findByVmUuidAndIsoUuid(@NotNull String vmUuid, @NotNull String isoUuid);
}
