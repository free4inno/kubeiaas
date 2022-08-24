package kubeiaas.dbproxy.dao;

import kubeiaas.dbproxy.table.IpSegmentTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IpSegmentDao extends JpaRepository<IpSegmentTable, Integer>, JpaSpecificationExecutor<IpSegmentTable> {

}
