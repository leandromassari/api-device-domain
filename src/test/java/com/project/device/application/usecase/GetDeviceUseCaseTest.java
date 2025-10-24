package com.project.device.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.device.domain.exception.DeviceNotFoundException;
import com.project.device.domain.model.Device;
import com.project.device.domain.model.DeviceState;
import com.project.device.domain.port.DeviceRepositoryPort;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
@DisplayName("GetDeviceUseCase Tests")
class GetDeviceUseCaseTest {

  @Mock private DeviceRepositoryPort deviceRepository;

  @InjectMocks private GetDeviceUseCase getDeviceUseCase;

  private UUID deviceId;
  private Device device1;
  private Device device2;
  private Device device3;

  @BeforeEach
  void setUp() {
    deviceId = UUID.randomUUID();
    LocalDateTime creationTime = LocalDateTime.now();

    device1 = new Device(deviceId, "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);
    device2 =
        new Device(
            UUID.randomUUID(), "Samsung Galaxy", "Samsung", DeviceState.IN_USE, creationTime);
    device3 =
        new Device(UUID.randomUUID(), "iPad Pro", "Apple", DeviceState.INACTIVE, creationTime);
  }

  @Nested
  @DisplayName("Find By ID Tests")
  class FindByIdTests {

    @Test
    @DisplayName("Should return device when found by ID")
    void shouldReturnDeviceWhenFoundById() {
      when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device1));

      Device result = getDeviceUseCase.findById(deviceId);

      assertNotNull(result);
      assertEquals(deviceId, result.getId());
      assertEquals("iPhone 14", result.getName());
      assertEquals("Apple", result.getBrand());
      assertEquals(DeviceState.AVAILABLE, result.getState());
      verify(deviceRepository, times(1)).findById(deviceId);
    }

    @Test
    @DisplayName("Should throw DeviceNotFoundException when device not found by ID")
    void shouldThrowExceptionWhenDeviceNotFoundById() {
      UUID nonExistentId = UUID.randomUUID();
      when(deviceRepository.findById(nonExistentId)).thenReturn(Optional.empty());

      DeviceNotFoundException exception =
          assertThrows(
              DeviceNotFoundException.class, () -> getDeviceUseCase.findById(nonExistentId));

      assertEquals("Device not found with id: " + nonExistentId, exception.getMessage());
      verify(deviceRepository, times(1)).findById(nonExistentId);
    }
  }

  @Nested
  @DisplayName("Find All Tests")
  class FindAllTests {

    @Test
    @DisplayName("Should return all devices when they exist")
    void shouldReturnAllDevicesWhenTheyExist() {
      List<Device> allDevices = Arrays.asList(device1, device2, device3);
      when(deviceRepository.findAll()).thenReturn(allDevices);

      List<Device> result = getDeviceUseCase.findAll();

      assertNotNull(result);
      assertEquals(3, result.size());
      assertTrue(result.contains(device1));
      assertTrue(result.contains(device2));
      assertTrue(result.contains(device3));
      verify(deviceRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no devices exist")
    void shouldReturnEmptyListWhenNoDevicesExist() {
      when(deviceRepository.findAll()).thenReturn(Collections.emptyList());

      List<Device> result = getDeviceUseCase.findAll();

      assertNotNull(result);
      assertTrue(result.isEmpty());
      verify(deviceRepository, times(1)).findAll();
    }
  }

  @Nested
  @DisplayName("Find By Brand Tests")
  class FindByBrandTests {

    @Test
    @DisplayName("Should return devices when found by brand")
    void shouldReturnDevicesWhenFoundByBrand() {
      List<Device> appleDevices = Arrays.asList(device1, device3);
      when(deviceRepository.findByBrand("Apple")).thenReturn(appleDevices);

      List<Device> result = getDeviceUseCase.findByBrand("Apple");

      assertNotNull(result);
      assertEquals(2, result.size());
      assertTrue(result.contains(device1));
      assertTrue(result.contains(device3));
      verify(deviceRepository, times(1)).findByBrand("Apple");
    }

    @Test
    @DisplayName("Should return empty list when no devices found for brand")
    void shouldReturnEmptyListWhenNoDevicesFoundForBrand() {
      when(deviceRepository.findByBrand("Dell")).thenReturn(Collections.emptyList());

      List<Device> result = getDeviceUseCase.findByBrand("Dell");

      assertNotNull(result);
      assertTrue(result.isEmpty());
      verify(deviceRepository, times(1)).findByBrand("Dell");
    }

    @Test
    @DisplayName("Should throw exception when brand is null")
    void shouldThrowExceptionWhenBrandIsNull() {
      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> getDeviceUseCase.findByBrand(null));

      assertEquals("Brand cannot be null or empty", exception.getMessage());
      verify(deviceRepository, times(0)).findByBrand(null);
    }

    @Test
    @DisplayName("Should throw exception when brand is empty")
    void shouldThrowExceptionWhenBrandIsEmpty() {
      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> getDeviceUseCase.findByBrand(""));

      assertEquals("Brand cannot be null or empty", exception.getMessage());
      verify(deviceRepository, times(0)).findByBrand("");
    }

    @Test
    @DisplayName("Should throw exception when brand is whitespace only")
    void shouldThrowExceptionWhenBrandIsWhitespace() {
      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> getDeviceUseCase.findByBrand("   "));

      assertEquals("Brand cannot be null or empty", exception.getMessage());
      verify(deviceRepository, times(0)).findByBrand("   ");
    }
  }

  @Nested
  @DisplayName("Find By State Tests")
  class FindByStateTests {

    @Test
    @DisplayName("Should return devices when found by AVAILABLE state")
    void shouldReturnDevicesWhenFoundByAvailableState() {
      when(deviceRepository.findByState(DeviceState.AVAILABLE))
          .thenReturn(Collections.singletonList(device1));

      List<Device> result = getDeviceUseCase.findByState(DeviceState.AVAILABLE);

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(device1, result.get(0));
      verify(deviceRepository, times(1)).findByState(DeviceState.AVAILABLE);
    }

    @Test
    @DisplayName("Should return devices when found by IN_USE state")
    void shouldReturnDevicesWhenFoundByInUseState() {
      when(deviceRepository.findByState(DeviceState.IN_USE))
          .thenReturn(Collections.singletonList(device2));

      List<Device> result = getDeviceUseCase.findByState(DeviceState.IN_USE);

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(device2, result.get(0));
      verify(deviceRepository, times(1)).findByState(DeviceState.IN_USE);
    }

    @Test
    @DisplayName("Should return devices when found by INACTIVE state")
    void shouldReturnDevicesWhenFoundByInactiveState() {
      when(deviceRepository.findByState(DeviceState.INACTIVE))
          .thenReturn(Collections.singletonList(device3));

      List<Device> result = getDeviceUseCase.findByState(DeviceState.INACTIVE);

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(device3, result.get(0));
      verify(deviceRepository, times(1)).findByState(DeviceState.INACTIVE);
    }

    @Test
    @DisplayName("Should return empty list when no devices found for state")
    void shouldReturnEmptyListWhenNoDevicesFoundForState() {
      when(deviceRepository.findByState(DeviceState.IN_USE)).thenReturn(Collections.emptyList());

      List<Device> result = getDeviceUseCase.findByState(DeviceState.IN_USE);

      assertNotNull(result);
      assertTrue(result.isEmpty());
      verify(deviceRepository, times(1)).findByState(DeviceState.IN_USE);
    }

    @Test
    @DisplayName("Should throw exception when state is null")
    void shouldThrowExceptionWhenStateIsNull() {
      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> getDeviceUseCase.findByState(null));

      assertEquals("State cannot be null", exception.getMessage());
      verify(deviceRepository, times(0)).findByState(null);
    }
  }
}
