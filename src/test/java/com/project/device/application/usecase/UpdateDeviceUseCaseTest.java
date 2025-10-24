package com.project.device.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.device.domain.exception.DeviceNotFoundException;
import com.project.device.domain.exception.InvalidDeviceOperationException;
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
@DisplayName("UpdateDeviceUseCase Tests")
class UpdateDeviceUseCaseTest {

  @Mock private DeviceRepositoryPort deviceRepository;

  @InjectMocks private UpdateDeviceUseCase updateDeviceUseCase;

  private UUID deviceId;
  private Device existingDevice;
  private LocalDateTime creationTime;

  @BeforeEach
  void setUp() {
    deviceId = UUID.randomUUID();
    creationTime = LocalDateTime.now().minusDays(1);
    existingDevice =
        new Device(deviceId, "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);
  }

  @Nested
  @DisplayName("Full Update Tests - Successful Cases")
  class FullUpdateSuccessTests {

    @Test
    @DisplayName("Should perform full update when device is AVAILABLE")
    void shouldPerformFullUpdateWhenAvailable() {
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
      when(deviceRepository.save(any(Device.class))).thenReturn(existingDevice);

      Device result =
          updateDeviceUseCase.fullUpdate(deviceId, "iPhone 15", "Apple", DeviceState.IN_USE);

      assertNotNull(result);
      verify(deviceRepository, times(1)).findById(deviceId);
      verify(deviceRepository, times(1)).save(any(Device.class));
    }

    @Test
    @DisplayName("Should update all fields in full update")
    void shouldUpdateAllFieldsInFullUpdate() {
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
      when(deviceRepository.save(any(Device.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Device result =
          updateDeviceUseCase.fullUpdate(
              deviceId, "Samsung Galaxy", "Samsung", DeviceState.INACTIVE);

      assertEquals("Samsung Galaxy", result.getName());
      assertEquals("Samsung", result.getBrand());
      assertEquals(DeviceState.INACTIVE, result.getState());
      assertEquals(creationTime, result.getCreationTime(), "Creation time should not change");
    }

    @Test
    @DisplayName("Should allow state change from AVAILABLE to IN_USE")
    void shouldAllowStateChangeFromAvailableToInUse() {
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
      when(deviceRepository.save(any(Device.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Device result =
          updateDeviceUseCase.fullUpdate(deviceId, "iPhone 14", "Apple", DeviceState.IN_USE);

      assertEquals(DeviceState.IN_USE, result.getState());
    }

    @Test
    @DisplayName("Should allow state change from IN_USE to AVAILABLE using partial update")
    void shouldAllowStateChangeFromInUseToAvailable() {
      existingDevice.setState(DeviceState.IN_USE);
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
      when(deviceRepository.save(any(Device.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Device result =
          updateDeviceUseCase.partialUpdate(deviceId, null, null, DeviceState.AVAILABLE);

      assertEquals(DeviceState.AVAILABLE, result.getState());
    }
  }

  @Nested
  @DisplayName("Partial Update Tests - Successful Cases")
  class PartialUpdateSuccessTests {

    @Test
    @DisplayName("Should update only name when brand and state are null")
    void shouldUpdateOnlyName() {
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
      when(deviceRepository.save(any(Device.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Device result = updateDeviceUseCase.partialUpdate(deviceId, "iPhone 15", null, null);

      assertEquals("iPhone 15", result.getName());
      assertEquals("Apple", result.getBrand(), "Brand should remain unchanged");
      assertEquals(DeviceState.AVAILABLE, result.getState(), "State should remain unchanged");
    }

    @Test
    @DisplayName("Should update only brand when name and state are null")
    void shouldUpdateOnlyBrand() {
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
      when(deviceRepository.save(any(Device.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Device result = updateDeviceUseCase.partialUpdate(deviceId, null, "Samsung", null);

      assertEquals("iPhone 14", result.getName(), "Name should remain unchanged");
      assertEquals("Samsung", result.getBrand());
      assertEquals(DeviceState.AVAILABLE, result.getState(), "State should remain unchanged");
    }

    @Test
    @DisplayName("Should update only state when name and brand are null")
    void shouldUpdateOnlyState() {
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
      when(deviceRepository.save(any(Device.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Device result = updateDeviceUseCase.partialUpdate(deviceId, null, null, DeviceState.IN_USE);

      assertEquals("iPhone 14", result.getName(), "Name should remain unchanged");
      assertEquals("Apple", result.getBrand(), "Brand should remain unchanged");
      assertEquals(DeviceState.IN_USE, result.getState());
    }

    @Test
    @DisplayName("Should update all fields when all parameters are provided")
    void shouldUpdateAllFieldsWhenAllProvided() {
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
      when(deviceRepository.save(any(Device.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Device result =
          updateDeviceUseCase.partialUpdate(
              deviceId, "Samsung Galaxy", "Samsung", DeviceState.INACTIVE);

      assertEquals("Samsung Galaxy", result.getName());
      assertEquals("Samsung", result.getBrand());
      assertEquals(DeviceState.INACTIVE, result.getState());
    }
  }

  @Nested
  @DisplayName("Business Rule - Cannot Update Name/Brand When IN_USE")
  class CannotUpdateNameBrandWhenInUseTests {

    @BeforeEach
    void setUp() {
      existingDevice.setState(DeviceState.IN_USE);
    }

    @Test
    @DisplayName("Should throw exception when trying to update name on IN_USE device (full update)")
    void shouldThrowExceptionWhenUpdatingNameOnInUseDeviceFullUpdate() {
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));

      InvalidDeviceOperationException exception =
          assertThrows(
              InvalidDeviceOperationException.class,
              () ->
                  updateDeviceUseCase.fullUpdate(
                      deviceId, "New Name", "Apple", DeviceState.IN_USE));

      assertEquals(
          "Cannot update name or brand of device in IN_USE state: " + deviceId,
          exception.getMessage());
      verify(deviceRepository, times(0)).save(any(Device.class));
    }

    @Test
    @DisplayName(
        "Should throw exception when trying to update brand on IN_USE device (full update)")
    void shouldThrowExceptionWhenUpdatingBrandOnInUseDeviceFullUpdate() {
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));

      InvalidDeviceOperationException exception =
          assertThrows(
              InvalidDeviceOperationException.class,
              () ->
                  updateDeviceUseCase.fullUpdate(
                      deviceId, "iPhone 14", "New Brand", DeviceState.IN_USE));

      assertEquals(
          "Cannot update name or brand of device in IN_USE state: " + deviceId,
          exception.getMessage());
      verify(deviceRepository, times(0)).save(any(Device.class));
    }

    @Test
    @DisplayName(
        "Should throw exception when trying to update name on IN_USE device (partial update)")
    void shouldThrowExceptionWhenUpdatingNameOnInUseDevicePartialUpdate() {
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));

      InvalidDeviceOperationException exception =
          assertThrows(
              InvalidDeviceOperationException.class,
              () -> updateDeviceUseCase.partialUpdate(deviceId, "New Name", null, null));

      assertEquals(
          "Cannot update name or brand of device in IN_USE state: " + deviceId,
          exception.getMessage());
      verify(deviceRepository, times(0)).save(any(Device.class));
    }

    @Test
    @DisplayName(
        "Should throw exception when trying to update brand on IN_USE device (partial update)")
    void shouldThrowExceptionWhenUpdatingBrandOnInUseDevicePartialUpdate() {
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));

      InvalidDeviceOperationException exception =
          assertThrows(
              InvalidDeviceOperationException.class,
              () -> updateDeviceUseCase.partialUpdate(deviceId, null, "New Brand", null));

      assertEquals(
          "Cannot update name or brand of device in IN_USE state: " + deviceId,
          exception.getMessage());
      verify(deviceRepository, times(0)).save(any(Device.class));
    }

    @Test
    @DisplayName("Should allow state update on IN_USE device without updating name or brand")
    void shouldAllowStateUpdateOnInUseDevice() {
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
      when(deviceRepository.save(any(Device.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Device result =
          updateDeviceUseCase.partialUpdate(deviceId, null, null, DeviceState.AVAILABLE);

      assertEquals(DeviceState.AVAILABLE, result.getState());
      verify(deviceRepository, times(1)).save(any(Device.class));
    }
  }

  @Nested
  @DisplayName("Business Rule - Invalid State Transitions")
  class InvalidStateTransitionTests {

    @Test
    @DisplayName("Should throw exception when transitioning from INACTIVE to IN_USE")
    void shouldThrowExceptionWhenTransitioningInactiveToInUse() {
      existingDevice.setState(DeviceState.INACTIVE);
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));

      InvalidDeviceOperationException exception =
          assertThrows(
              InvalidDeviceOperationException.class,
              () ->
                  updateDeviceUseCase.fullUpdate(
                      deviceId, "iPhone 14", "Apple", DeviceState.IN_USE));

      assertEquals(
          String.format(
              "Invalid state transition from %s to %s for device: %s",
              DeviceState.INACTIVE, DeviceState.IN_USE, deviceId),
          exception.getMessage());
      verify(deviceRepository, times(0)).save(any(Device.class));
    }

    @Test
    @DisplayName("Should throw exception for invalid state transition in partial update")
    void shouldThrowExceptionForInvalidStateTransitionPartialUpdate() {
      existingDevice.setState(DeviceState.INACTIVE);
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));

      InvalidDeviceOperationException exception =
          assertThrows(
              InvalidDeviceOperationException.class,
              () -> updateDeviceUseCase.partialUpdate(deviceId, null, null, DeviceState.IN_USE));

      assertEquals(
          String.format(
              "Invalid state transition from %s to %s for device: %s",
              DeviceState.INACTIVE, DeviceState.IN_USE, deviceId),
          exception.getMessage());
      verify(deviceRepository, times(0)).save(any(Device.class));
    }
  }

  @Nested
  @DisplayName("Device Not Found Tests")
  class DeviceNotFoundTests {

    @Test
    @DisplayName("Should throw exception when device not found (full update)")
    void shouldThrowExceptionWhenDeviceNotFoundFullUpdate() {
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

      DeviceNotFoundException exception =
          assertThrows(
              DeviceNotFoundException.class,
              () ->
                  updateDeviceUseCase.fullUpdate(
                      deviceId, "iPhone 15", "Apple", DeviceState.AVAILABLE));

      assertEquals("Device not found with id: " + deviceId, exception.getMessage());
      verify(deviceRepository, times(0)).save(any(Device.class));
    }

    @Test
    @DisplayName("Should throw exception when device not found (partial update)")
    void shouldThrowExceptionWhenDeviceNotFoundPartialUpdate() {
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

      DeviceNotFoundException exception =
          assertThrows(
              DeviceNotFoundException.class,
              () -> updateDeviceUseCase.partialUpdate(deviceId, "New Name", null, null));

      assertEquals("Device not found with id: " + deviceId, exception.getMessage());
      verify(deviceRepository, times(0)).save(any(Device.class));
    }
  }

  @Nested
  @DisplayName("Creation Time Immutability Tests")
  class CreationTimeImmutabilityTests {

    @Test
    @DisplayName("Should not modify creation time during full update")
    void shouldNotModifyCreationTimeDuringFullUpdate() {
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
      when(deviceRepository.save(any(Device.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Device result =
          updateDeviceUseCase.fullUpdate(
              deviceId, "Updated Name", "Updated Brand", DeviceState.INACTIVE);

      assertEquals(
          creationTime,
          result.getCreationTime(),
          "Creation time should remain unchanged after update");
    }

    @Test
    @DisplayName("Should not modify creation time during partial update")
    void shouldNotModifyCreationTimeDuringPartialUpdate() {
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
      when(deviceRepository.save(any(Device.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Device result = updateDeviceUseCase.partialUpdate(deviceId, "Updated Name", null, null);

      assertEquals(
          creationTime,
          result.getCreationTime(),
          "Creation time should remain unchanged after partial update");
    }
  }
}
