package com.project.device.application.usecase;

import com.project.device.domain.exception.DeviceNotFoundException;
import com.project.device.domain.exception.InvalidDeviceOperationException;
import com.project.device.domain.model.Device;
import com.project.device.domain.model.DeviceState;
import com.project.device.domain.port.DeviceRepositoryPort;
import java.util.UUID;

/**
 * Use case for updating existing devices.
 *
 * <p>This use case handles both full and partial updates of device information, enforcing business
 * rules such as preventing updates to devices in use and maintaining creation time immutability.
 *
 * <p>Follows the Single Responsibility Principle by focusing solely on device update logic.
 */
public class UpdateDeviceUseCase {

  private final DeviceRepositoryPort deviceRepository;

  /**
   * Constructs a new UpdateDeviceUseCase with the required repository.
   *
   * @param deviceRepository the repository port for device persistence operations
   */
  public UpdateDeviceUseCase(DeviceRepositoryPort deviceRepository) {
    this.deviceRepository = deviceRepository;
  }

  /**
   * Performs a full update of a device, replacing all mutable fields.
   *
   * <p>Business rules enforced: - Creation time cannot be modified - Name and brand cannot be
   * updated if device is IN_USE - State transitions must be valid
   *
   * @param id the UUID of the device to update
   * @param name the new name for the device
   * @param brand the new brand for the device
   * @param state the new state for the device
   * @return the updated device
   * @throws DeviceNotFoundException if device with given ID doesn't exist
   * @throws InvalidDeviceOperationException if business rules are violated
   */
  public Device fullUpdate(UUID id, String name, String brand, DeviceState state) {
    Device existingDevice =
        deviceRepository.findById(id).orElseThrow(() -> new DeviceNotFoundException(id));

    validateNameAndBrandUpdate(existingDevice, name, brand);
    validateStateTransition(existingDevice, state);

    existingDevice.setName(name);
    existingDevice.setBrand(brand);
    existingDevice.setState(state);

    return deviceRepository.save(existingDevice);
  }

  /**
   * Performs a partial update of a device, updating only specified fields.
   *
   * <p>Business rules enforced: - Creation time cannot be modified - Name and brand cannot be
   * updated if device is IN_USE - State transitions must be valid - Null values are ignored (field
   * not updated)
   *
   * @param id the UUID of the device to update
   * @param name the new name for the device (null to keep current)
   * @param brand the new brand for the device (null to keep current)
   * @param state the new state for the device (null to keep current)
   * @return the updated device
   * @throws DeviceNotFoundException if device with given ID doesn't exist
   * @throws InvalidDeviceOperationException if business rules are violated
   */
  public Device partialUpdate(UUID id, String name, String brand, DeviceState state) {
    Device existingDevice =
        deviceRepository.findById(id).orElseThrow(() -> new DeviceNotFoundException(id));

    if (name != null || brand != null) {
      validateNameAndBrandUpdate(existingDevice, name, brand);
    }

    if (name != null) {
      existingDevice.setName(name);
    }

    if (brand != null) {
      existingDevice.setBrand(brand);
    }

    if (state != null) {
      validateStateTransition(existingDevice, state);
      existingDevice.setState(state);
    }

    return deviceRepository.save(existingDevice);
  }

  private void validateNameAndBrandUpdate(Device device, String name, String brand) {
    if (!device.canUpdateNameOrBrand()) {
      throw new InvalidDeviceOperationException(
          "Cannot update name or brand of device in IN_USE state: " + device.getId());
    }
  }

  private void validateStateTransition(Device device, DeviceState newState) {
    if (!device.validateStateTransition(newState)) {
      throw new InvalidDeviceOperationException(
          String.format(
              "Invalid state transition from %s to %s for device: %s",
              device.getState(), newState, device.getId()));
    }
  }
}
