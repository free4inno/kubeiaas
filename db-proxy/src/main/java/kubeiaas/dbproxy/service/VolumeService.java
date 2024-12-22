package kubeiaas.dbproxy.service;

import kubeiaas.dbproxy.dao.VolumeDao;
import kubeiaas.dbproxy.table.VolumeTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

@Slf4j
@Service
public class VolumeService {
    @Resource
    private VolumeDao volumeDao;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public VolumeTable safeUpdate(VolumeTable volumeTable) {
        try {
            // If the entity is new, just save it
            if (volumeTable.getId() == null) {
                return volumeDao.saveAndFlush(volumeTable);
            }

            // If the entity exists, fetch it first to get it into the persistence context
            VolumeTable existingVolumeTable = volumeDao.findById(volumeTable.getId()).orElse(null);
            // if the entity doesn't exist, it will be created.
            if (existingVolumeTable == null) {
                return volumeDao.saveAndFlush(volumeTable);
            }

            // If entity exists, copy the updated fields from newEntity into existingEntity
            // Assume copyUpdatedFields is a method that sets the updated values from newEntity to existingEntity
            copyUpdatedFields(volumeTable, existingVolumeTable);

            // Hibernate will automatically update the entity because it's managed
            // Explicitly flushing to make sure changes are applied
            entityManager.flush();

            return existingVolumeTable;
        } catch (Exception e) {
            // Handle exceptions as needed, could be optimistic lock exception, constraint violations, etc.
            // Could also log the error here
            throw new RuntimeException("Failed to update entity", e);
        }
    }

    private void copyUpdatedFields(VolumeTable newVolumeTable, VolumeTable existingVolumeTable) {
        // Copy fields(except for id) from newVolumeTable to existingVolumeTable
        // existingVolumeTable.setField(newVolumeTable.getField());
        existingVolumeTable.setUuid(newVolumeTable.getUuid());
        existingVolumeTable.setName(newVolumeTable.getName());
        existingVolumeTable.setDescription(newVolumeTable.getDescription());
        existingVolumeTable.setHostUuid(newVolumeTable.getHostUuid());
        existingVolumeTable.setImageUuid(newVolumeTable.getImageUuid());
        existingVolumeTable.setSize(newVolumeTable.getSize());
        existingVolumeTable.setProviderLocation(newVolumeTable.getProviderLocation());
        existingVolumeTable.setFormatType(newVolumeTable.getFormatType());
        existingVolumeTable.setUsageType(newVolumeTable.getUsageType());
        existingVolumeTable.setStatus(newVolumeTable.getStatus());
        existingVolumeTable.setInstanceUuid(newVolumeTable.getInstanceUuid());
        existingVolumeTable.setMountPoint(newVolumeTable.getMountPoint());
        existingVolumeTable.setBus(newVolumeTable.getBus());
        existingVolumeTable.setCreateTime(newVolumeTable.getCreateTime());
    }
}
