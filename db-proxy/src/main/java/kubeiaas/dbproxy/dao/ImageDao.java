package kubeiaas.dbproxy.dao;

import kubeiaas.dbproxy.table.ImageTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ImageDao extends JpaRepository<ImageTable, Integer>, JpaSpecificationExecutor<ImageTable> {

}
