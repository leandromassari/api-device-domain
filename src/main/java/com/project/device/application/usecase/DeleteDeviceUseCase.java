package com.project.device.application.usecase;

import com.project.device.domain.exception.DeviceInUseException;
import com.project.device.domain.exception.DeviceNotFoundException;
import com.project.device.domain.model.Device;
import com.project.device.domain.port.DeviceRepositoryPort;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Use case for deleting devices.
 *
 * <p>This use case handles device deletion with proper business rule validation, ensuring that
 * devices currently in use cannot be deleted to maintain data integrity.
 *
 * <p>Follows the Single Responsibility Principle by focusing solely on device deletion logic.
 */
@Service
public class DeleteDeviceUseCase {

  private final DeviceRepositoryPort deviceRepository;

  /**
   * Constructs a new DeleteDeviceUseCase with the required repository.
   *
   * @param deviceRepository the repository port for device persistence operations
   */
  public DeleteDeviceUseCase(DeviceRepositoryPort deviceRepository) {
    this.deviceRepository = deviceRepository;
  }

  /**
   * Deletes a device by its unique identifier.
   *
   * <p>Business rules enforced: - Device must exist - Device cannot be deleted if it is in IN_USE
   * state
   *
   * @param id the UUID of the device to delete
   * @throws DeviceNotFoundException if no device exists with the given ID
   * @throws DeviceInUseException if the device is currently in use (IN_USE state)
   */
  public void execute(UUID id) {
    Device device =
        deviceRepository.findById(id).orElseThrow(() -> new DeviceNotFoundException(id));

    validateDeletion(device);

    deviceRepository.deleteById(id);
  }

  private void validateDeletion(Device device) {
    if (!device.canDelete()) {
      throw new DeviceInUseException(device.getId());
    }
  }
}
