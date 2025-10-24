package com.project.device.infrastructure.adapter.rest;

import com.project.device.domain.model.Device;
import com.project.device.infrastructure.adapter.rest.dto.DeviceResponse;

/**
 * Utility class for mapping between Device domain model and REST DTOs.
 *
 * <p>This mapper provides conversion from domain models to REST response DTOs.
 */
public final class DeviceMapper {

  private DeviceMapper() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Converts a Device domain model to a DeviceResponse DTO.
   *
   * @param device the domain model to convert
   * @return the corresponding DeviceResponse DTO
   */
  public static DeviceResponse toResponse(Device device) {
    if (device == null) {
      return null;
    }

    return new DeviceResponse(
        device.getId(),
        device.getName(),
        device.getBrand(),
        device.getState(),
        device.getCreationTime());
  }
}
