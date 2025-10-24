package com.project.device.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.device.domain.model.Device;
import com.project.device.domain.model.DeviceState;
import com.project.device.domain.port.DeviceRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateDeviceUseCase Tests")
class CreateDeviceUseCaseTest {

  @Mock private DeviceRepositoryPort deviceRepository;

  @InjectMocks private CreateDeviceUseCase createDeviceUseCase;

  private Device savedDevice;

  @BeforeEach
  void setUp() {
    savedDevice = new Device();
    savedDevice.setName("iPhone 14");
    savedDevice.setBrand("Apple");
    savedDevice.setState(DeviceState.AVAILABLE);
  }

  @Nested
  @DisplayName("Successful Device Creation Tests")
  class SuccessfulCreationTests {

    @Test
    @DisplayName("Should create device successfully with valid inputs")
    void shouldCreateDeviceSuccessfully() {
      when(deviceRepository.save(any(Device.class))).thenReturn(savedDevice);

      Device result = createDeviceUseCase.execute("iPhone 14", "Apple", DeviceState.AVAILABLE);

      assertNotNull(result);
      assertEquals("iPhone 14", result.getName());
      assertEquals("Apple", result.getBrand());
      assertEquals(DeviceState.AVAILABLE, result.getState());
      verify(deviceRepository, times(1)).save(any(Device.class));
    }

    @Test
    @DisplayName("Should generate UUID for new device")
    void shouldGenerateUuidForNewDevice() {
      ArgumentCaptor<Device> deviceCaptor = ArgumentCaptor.forClass(Device.class);
      when(deviceRepository.save(any(Device.class))).thenReturn(savedDevice);

      createDeviceUseCase.execute("Samsung Galaxy", "Samsung", DeviceState.IN_USE);

      verify(deviceRepository).save(deviceCaptor.capture());
      Device capturedDevice = deviceCaptor.getValue();

      assertNotNull(capturedDevice.getId(), "Device ID should be generated");
    }

    @Test
    @DisplayName("Should set creation time for new device")
    void shouldSetCreationTimeForNewDevice() {
      ArgumentCaptor<Device> deviceCaptor = ArgumentCaptor.forClass(Device.class);
      when(deviceRepository.save(any(Device.class))).thenReturn(savedDevice);

      createDeviceUseCase.execute("Dell Laptop", "Dell", DeviceState.AVAILABLE);

      verify(deviceRepository).save(deviceCaptor.capture());
      Device capturedDevice = deviceCaptor.getValue();

      assertNotNull(capturedDevice.getCreationTime(), "Creation time should be set");
    }

    @Test
    @DisplayName("Should create device with AVAILABLE state")
    void shouldCreateDeviceWithAvailableState() {
      when(deviceRepository.save(any(Device.class))).thenReturn(savedDevice);

      Device result = createDeviceUseCase.execute("HP Printer", "HP", DeviceState.AVAILABLE);

      assertNotNull(result);
      verify(deviceRepository, times(1)).save(any(Device.class));
    }

    @Test
    @DisplayName("Should create device with IN_USE state")
    void shouldCreateDeviceWithInUseState() {
      savedDevice.setState(DeviceState.IN_USE);
      when(deviceRepository.save(any(Device.class))).thenReturn(savedDevice);

      Device result = createDeviceUseCase.execute("Lenovo ThinkPad", "Lenovo", DeviceState.IN_USE);

      assertEquals(DeviceState.IN_USE, result.getState());
      verify(deviceRepository, times(1)).save(any(Device.class));
    }

    @Test
    @DisplayName("Should create device with INACTIVE state")
    void shouldCreateDeviceWithInactiveState() {
      savedDevice.setState(DeviceState.INACTIVE);
      when(deviceRepository.save(any(Device.class))).thenReturn(savedDevice);

      Device result = createDeviceUseCase.execute("Canon Camera", "Canon", DeviceState.INACTIVE);

      assertEquals(DeviceState.INACTIVE, result.getState());
      verify(deviceRepository, times(1)).save(any(Device.class));
    }
  }

  @Nested
  @DisplayName("Validation Tests - Name Field")
  class NameValidationTests {

    @Test
    @DisplayName("Should throw exception when name is null")
    void shouldThrowExceptionWhenNameIsNull() {
      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> createDeviceUseCase.execute(null, "Apple", DeviceState.AVAILABLE));

      assertEquals("Device name cannot be null or empty", exception.getMessage());
      verify(deviceRepository, times(0)).save(any(Device.class));
    }

    @Test
    @DisplayName("Should throw exception when name is empty string")
    void shouldThrowExceptionWhenNameIsEmpty() {
      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> createDeviceUseCase.execute("", "Apple", DeviceState.AVAILABLE));

      assertEquals("Device name cannot be null or empty", exception.getMessage());
      verify(deviceRepository, times(0)).save(any(Device.class));
    }

    @Test
    @DisplayName("Should throw exception when name is whitespace only")
    void shouldThrowExceptionWhenNameIsWhitespace() {
      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> createDeviceUseCase.execute("   ", "Apple", DeviceState.AVAILABLE));

      assertEquals("Device name cannot be null or empty", exception.getMessage());
      verify(deviceRepository, times(0)).save(any(Device.class));
    }
  }

  @Nested
  @DisplayName("Validation Tests - Brand Field")
  class BrandValidationTests {

    @Test
    @DisplayName("Should throw exception when brand is null")
    void shouldThrowExceptionWhenBrandIsNull() {
      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> createDeviceUseCase.execute("iPhone 14", null, DeviceState.AVAILABLE));

      assertEquals("Device brand cannot be null or empty", exception.getMessage());
      verify(deviceRepository, times(0)).save(any(Device.class));
    }

    @Test
    @DisplayName("Should throw exception when brand is empty string")
    void shouldThrowExceptionWhenBrandIsEmpty() {
      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> createDeviceUseCase.execute("iPhone 14", "", DeviceState.AVAILABLE));

      assertEquals("Device brand cannot be null or empty", exception.getMessage());
      verify(deviceRepository, times(0)).save(any(Device.class));
    }

    @Test
    @DisplayName("Should throw exception when brand is whitespace only")
    void shouldThrowExceptionWhenBrandIsWhitespace() {
      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> createDeviceUseCase.execute("iPhone 14", "   ", DeviceState.AVAILABLE));

      assertEquals("Device brand cannot be null or empty", exception.getMessage());
      verify(deviceRepository, times(0)).save(any(Device.class));
    }
  }

  @Nested
  @DisplayName("Validation Tests - State Field")
  class StateValidationTests {

    @Test
    @DisplayName("Should throw exception when state is null")
    void shouldThrowExceptionWhenStateIsNull() {
      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> createDeviceUseCase.execute("iPhone 14", "Apple", null));

      assertEquals("Device state cannot be null", exception.getMessage());
      verify(deviceRepository, times(0)).save(any(Device.class));
    }
  }
}
