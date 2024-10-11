package kubeiaas.dbproxy.dao;

import kubeiaas.dbproxy.table.HostBridgeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface HostBridgeDao extends JpaRepository<HostBridgeTable, Integer>, JpaSpecificationExecutor<HostBridgeTable> {

    @Transactional
    @Query(value = "INSERT INTO host_bridge (bridge_name, host_uuid, ip_segment_id) values ('eno1', '58b94fc0197f43c5bec07fe69be4b9c0', 68)", nativeQuery = true)
    void migrateFromIpSegment();

//    @Transactional
//    @Query(value = "INSERT INTO host_bridge (bridge_name, host_uuid, ip_segment_id) SELECT bridge, host_uuid, id FROM ip_segment", nativeQuery = true)
//    void migrateFromIpSegment();
}
