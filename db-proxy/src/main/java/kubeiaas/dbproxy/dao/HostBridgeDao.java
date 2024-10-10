package kubeiaas.dbproxy.dao;

import kubeiaas.dbproxy.table.HostBridgeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface HostBridgeDao extends JpaRepository<HostBridgeTable, Integer>, JpaSpecificationExecutor<HostBridgeTable> {

    @Query(value = "INSERT INTO host_bridge (bridge_name, host_uuid, ip_segment_id) SELECT bridge, host_uuid, id FROM ip_segment", nativeQuery = true)
    void migrateFromOldIpSegment();
}
