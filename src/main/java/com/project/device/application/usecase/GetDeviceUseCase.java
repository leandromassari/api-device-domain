package com.project.device.application.usecase;

import com.project.device.domain.exception.DeviceNotFoundException;
import com.project.device.domain.model.Device;
import com.project.device.domain.model.DeviceState;
import com.project.device.domain.port.DeviceRepositoryPort;
import java.util.List;
import java.util.UUID;

/**
 * Use case for retrieving device information.
 *
 * <p>This use case handles all read operations for devices, providing various query methods to
 * retrieve devices by different criteria.
 *
 * <p>Follows the Single Responsibility Principle by focusing solely on device retrieval operations.
 */
public class GetDeviceUseCase {

  private final DeviceRepositoryPort deviceRepository;

  /**
   * Constructs a new GetDeviceUseCase with the required repository.
   *
   * @param deviceRepository the repository port for device persistence operations
   */
  public GetDeviceUseCase(DeviceRepositoryPort deviceRepository) {
    this.deviceRepository = deviceRepository;
  }

  /**
   * Retrieves a device by its unique identifier.
   *
   * @param id the UUID of the device to retrieve
   * @return the device with the specified ID
   * @throws DeviceNotFoundException if no device exists with the given ID
   */
  public Device findById(UUID id) {
    return deviceRepository.findById(id).orElseThrow(() -> new DeviceNotFoundException(id));
  }

  /**
   * Retrieves all devices in the system.
   *
   * @return a list of all devices, empty list if no devices exist
   */
  public List<Device> findAll() {
    return deviceRepository.findAll();
  }

  /**
   * Retrieves all devices of a specific brand.
   *
   * @param brand the brand to filter by, must not be null or empty
   * @return a list of devices with the specified brand, empty list if none found
   * @throws IllegalArgumentException if brand is null or empty
   */
  public List<Device> findByBrand(String brand) {
    if (brand == null || brand.trim().isEmpty()) {
      throw new IllegalArgumentException("Brand cannot be null or empty");
    }
    return deviceRepository.findByBrand(brand);
  }

  /**
   * Retrieves all devices in a specific state.
   *
   * @param state the state to filter by, must not be null
   * @return a list of devices in the specified state, empty list if none found
   * @throws IllegalArgumentException if state is null
   */
  public List<Device> findByState(DeviceState state) {
    if (state == null) {
      throw new IllegalArgumentException("State cannot be null");
    }
    return deviceRepository.findByState(state);
  }
}
