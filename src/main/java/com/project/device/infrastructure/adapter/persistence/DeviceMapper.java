package com.project.device.infrastructure.adapter.persistence;

import com.project.device.domain.model.Device;

/**
 * Utility class for mapping between Device domain model and DeviceEntity.
 *
 * <p>This mapper provides bidirectional conversion between the domain layer and persistence layer
 * representations of devices.
 */
public final class DeviceMapper {

  private DeviceMapper() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Converts a Device domain model to a DeviceEntity.
   *
   * @param device the domain model to convert
   * @return the corresponding DeviceEntity
   */
  public static DeviceEntity toEntity(Device device) {
    if (device == null) {
      return null;
    }

    return new DeviceEntity(
        device.getId(),
        device.getName(),
        device.getBrand(),
        device.getState(),
        device.getCreationTime());
  }

  /**
   * Converts a DeviceEntity to a Device domain model.
   *
   * @param entity the entity to convert
   * @return the corresponding Device domain model
   */
  public static Device toDomain(DeviceEntity entity) {
    if (entity == null) {
      return null;
    }

    return new Device(
        entity.getId(),
        entity.getName(),
        entity.getBrand(),
        entity.getState(),
        entity.getCreationTime());
  }
}
