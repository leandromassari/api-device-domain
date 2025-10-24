package com.project.device.application.usecase;

import com.project.device.domain.model.Device;
import com.project.device.domain.model.DeviceState;
import com.project.device.domain.port.DeviceRepositoryPort;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Use case for creating a new device.
 *
 * <p>This use case handles the creation of new devices in the system, automatically generating
 * UUIDs and setting creation timestamps.
 *
 * <p>Follows the Single Responsibility Principle by focusing solely on device creation logic.
 */
@Service
public class CreateDeviceUseCase {

  private final DeviceRepositoryPort deviceRepository;

  /**
   * Constructs a new CreateDeviceUseCase with the required repository.
   *
   * @param deviceRepository the repository port for device persistence operations
   */
  public CreateDeviceUseCase(DeviceRepositoryPort deviceRepository) {
    this.deviceRepository = deviceRepository;
  }

  /**
   * Creates a new device with the specified attributes.
   *
   * <p>The device will be assigned a new UUID and the current timestamp as creation time.
   *
   * @param name the name of the device, must not be null or empty
   * @param brand the brand of the device, must not be null or empty
   * @param state the initial state of the device, must not be null
   * @return the created device with generated ID and creation time
   * @throws IllegalArgumentException if name, brand, or state is null or empty
   */
  public Device execute(String name, String brand, DeviceState state) {
    validateInput(name, brand, state);

    Device device = new Device();
    device.setId(UUID.randomUUID());
    device.setName(name);
    device.setBrand(brand);
    device.setState(state);
    device.setCreationTime(LocalDateTime.now());

    return deviceRepository.save(device);
  }

  private void validateInput(String name, String brand, DeviceState state) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Device name cannot be null or empty");
    }
    if (brand == null || brand.trim().isEmpty()) {
      throw new IllegalArgumentException("Device brand cannot be null or empty");
    }
    if (state == null) {
      throw new IllegalArgumentException("Device state cannot be null");
    }
  }
}
