package com.project.device.infrastructure.adapter.persistence;

import com.project.device.domain.model.DeviceState;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for DeviceEntity.
 *
 * <p>Provides CRUD operations and custom query methods for device persistence.
 */
@Repository
public interface DeviceJpaRepository extends JpaRepository<DeviceEntity, UUID> {

  /**
   * Finds all devices with the specified brand.
   *
   * @param brand the brand to search for
   * @return list of devices with matching brand
   */
  List<DeviceEntity> findByBrand(String brand);

  /**
   * Finds all devices in the specified state.
   *
   * @param state the state to search for
   * @return list of devices with matching state
   */
  List<DeviceEntity> findByState(DeviceState state);
}
