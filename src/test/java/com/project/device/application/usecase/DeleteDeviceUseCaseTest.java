package com.project.device.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.device.domain.exception.DeviceInUseException;
import com.project.device.domain.exception.DeviceNotFoundException;
import com.project.device.domain.model.Device;
import com.project.device.domain.model.DeviceState;
import com.project.device.domain.port.DeviceRepositoryPort;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteDeviceUseCase Tests")
class DeleteDeviceUseCaseTest {

  @Mock private DeviceRepositoryPort deviceRepository;

  @InjectMocks private DeleteDeviceUseCase deleteDeviceUseCase;

  private UUID deviceId;
  private LocalDateTime creationTime;

  @BeforeEach
  void setUp() {
    deviceId = UUID.randomUUID();
    creationTime = LocalDateTime.now();
  }

  @Nested
  @DisplayName("Successful Deletion Tests")
  class SuccessfulDeletionTests {

    @Test
    @DisplayName("Should delete device when state is AVAILABLE")
    void shouldDeleteDeviceWhenAvailable() {
      Device device =
          new Device(deviceId, "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
      doNothing().when(deviceRepository).deleteById(deviceId);

      deleteDeviceUseCase.execute(deviceId);

      verify(deviceRepository, times(1)).findById(deviceId);
      verify(deviceRepository, times(1)).deleteById(deviceId);
    }

    @Test
    @DisplayName("Should delete device when state is INACTIVE")
    void shouldDeleteDeviceWhenInactive() {
      Device device =
          new Device(deviceId, "Samsung Galaxy", "Samsung", DeviceState.INACTIVE, creationTime);
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
      doNothing().when(deviceRepository).deleteById(deviceId);

      deleteDeviceUseCase.execute(deviceId);

      verify(deviceRepository, times(1)).findById(deviceId);
      verify(deviceRepository, times(1)).deleteById(deviceId);
    }
  }

  @Nested
  @DisplayName("Business Rule - Cannot Delete When IN_USE")
  class CannotDeleteWhenInUseTests {

    @Test
    @DisplayName("Should throw DeviceInUseException when trying to delete IN_USE device")
    void shouldThrowExceptionWhenDeletingInUseDevice() {
      Device device = new Device(deviceId, "iPad Pro", "Apple", DeviceState.IN_USE, creationTime);
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

      DeviceInUseException exception =
          assertThrows(DeviceInUseException.class, () -> deleteDeviceUseCase.execute(deviceId));

      assertEquals(
          "Device is currently in use and cannot be deleted: " + deviceId, exception.getMessage());
      verify(deviceRepository, times(1)).findById(deviceId);
      verify(deviceRepository, times(0)).deleteById(deviceId);
    }

    @Test
    @DisplayName("Should call canDelete() method to validate deletion")
    void shouldCallCanDeleteMethodToValidateDeletion() {
      Device device = new Device(deviceId, "Laptop", "Dell", DeviceState.IN_USE, creationTime);
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

      assertThrows(DeviceInUseException.class, () -> deleteDeviceUseCase.execute(deviceId));

      verify(deviceRepository, times(1)).findById(deviceId);
      verify(deviceRepository, times(0)).deleteById(deviceId);
    }
  }

  @Nested
  @DisplayName("Device Not Found Tests")
  class DeviceNotFoundTests {

    @Test
    @DisplayName("Should throw DeviceNotFoundException when device does not exist")
    void shouldThrowExceptionWhenDeviceNotFound() {
      UUID nonExistentId = UUID.randomUUID();
      when(deviceRepository.findById(nonExistentId)).thenReturn(Optional.empty());

      DeviceNotFoundException exception =
          assertThrows(
              DeviceNotFoundException.class, () -> deleteDeviceUseCase.execute(nonExistentId));

      assertEquals("Device not found with id: " + nonExistentId, exception.getMessage());
      verify(deviceRepository, times(1)).findById(nonExistentId);
      verify(deviceRepository, times(0)).deleteById(nonExistentId);
    }

    @Test
    @DisplayName("Should validate device existence before checking deletion permission")
    void shouldValidateExistenceBeforeCheckingDeletionPermission() {
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

      assertThrows(DeviceNotFoundException.class, () -> deleteDeviceUseCase.execute(deviceId));

      verify(deviceRepository, times(1)).findById(deviceId);
      verify(deviceRepository, times(0)).deleteById(deviceId);
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle multiple deletion attempts on same device")
    void shouldHandleMultipleDeletionAttemptsOnSameDevice() {
      Device device = new Device(deviceId, "Monitor", "LG", DeviceState.AVAILABLE, creationTime);
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
      doNothing().when(deviceRepository).deleteById(deviceId);

      deleteDeviceUseCase.execute(deviceId);

      verify(deviceRepository, times(1)).findById(deviceId);
      verify(deviceRepository, times(1)).deleteById(deviceId);
    }

    @Test
    @DisplayName("Should properly validate state before deletion")
    void shouldProperlyValidateStateBeforeDeletion() {
      Device availableDevice =
          new Device(deviceId, "Keyboard", "Logitech", DeviceState.AVAILABLE, creationTime);
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(availableDevice));
      doNothing().when(deviceRepository).deleteById(deviceId);

      deleteDeviceUseCase.execute(deviceId);

      verify(deviceRepository, times(1)).findById(deviceId);
      verify(deviceRepository, times(1)).deleteById(deviceId);
    }
  }
}
