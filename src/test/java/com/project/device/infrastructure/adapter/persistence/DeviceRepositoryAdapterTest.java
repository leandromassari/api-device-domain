package com.project.device.infrastructure.adapter.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.device.domain.model.Device;
import com.project.device.domain.model.DeviceState;
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
@DisplayName("DeviceRepositoryAdapter Tests")
class DeviceRepositoryAdapterTest {

  @Mock private DeviceJpaRepository jpaRepository;

  @InjectMocks private DeviceRepositoryAdapter deviceRepositoryAdapter;

  private UUID deviceId;
  private LocalDateTime creationTime;
  private Device domainDevice;
  private DeviceEntity deviceEntity;

  @BeforeEach
  void setUp() {
    deviceId = UUID.randomUUID();
    creationTime = LocalDateTime.now();

    domainDevice = new Device(deviceId, "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);
    deviceEntity =
        new DeviceEntity(deviceId, "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);
  }

  @Nested
  @DisplayName("Save Operation Tests")
  class SaveOperationTests {

    @Test
    @DisplayName("Should save device and convert entity to domain model")
    void shouldSaveDeviceAndConvertToDomain() {
      when(jpaRepository.save(any(DeviceEntity.class))).thenReturn(deviceEntity);

      Device result = deviceRepositoryAdapter.save(domainDevice);

      assertNotNull(result);
      assertEquals(deviceId, result.getId());
      assertEquals("iPhone 14", result.getName());
      assertEquals("Apple", result.getBrand());
      assertEquals(DeviceState.AVAILABLE, result.getState());
      assertEquals(creationTime, result.getCreationTime());
      verify(jpaRepository, times(1)).save(any(DeviceEntity.class));
    }

    @Test
    @DisplayName("Should correctly map domain model to entity during save")
    void shouldCorrectlyMapDomainToEntityDuringSave() {
      when(jpaRepository.save(any(DeviceEntity.class))).thenReturn(deviceEntity);

      deviceRepositoryAdapter.save(domainDevice);

      verify(jpaRepository, times(1)).save(any(DeviceEntity.class));
    }
  }

  @Nested
  @DisplayName("Find By ID Tests")
  class FindByIdTests {

    @Test
    @DisplayName("Should find device by ID and convert to domain model")
    void shouldFindDeviceByIdAndConvertToDomain() {
      when(jpaRepository.findById(deviceId)).thenReturn(Optional.of(deviceEntity));

      Optional<Device> result = deviceRepositoryAdapter.findById(deviceId);

      assertTrue(result.isPresent());
      assertEquals(deviceId, result.get().getId());
      assertEquals("iPhone 14", result.get().getName());
      assertEquals("Apple", result.get().getBrand());
      assertEquals(DeviceState.AVAILABLE, result.get().getState());
      assertEquals(creationTime, result.get().getCreationTime());
      verify(jpaRepository, times(1)).findById(deviceId);
    }

    @Test
    @DisplayName("Should return empty optional when device not found by ID")
    void shouldReturnEmptyOptionalWhenNotFound() {
      UUID nonExistentId = UUID.randomUUID();
      when(jpaRepository.findById(nonExistentId)).thenReturn(Optional.empty());

      Optional<Device> result = deviceRepositoryAdapter.findById(nonExistentId);

      assertFalse(result.isPresent());
      verify(jpaRepository, times(1)).findById(nonExistentId);
    }
  }

  @Nested
  @DisplayName("Find All Tests")
  class FindAllTests {

    @Test
    @DisplayName("Should find all devices and convert to domain models")
    void shouldFindAllDevicesAndConvertToDomain() {
      DeviceEntity entity2 =
          new DeviceEntity(
              UUID.randomUUID(), "Samsung Galaxy", "Samsung", DeviceState.IN_USE, creationTime);
      DeviceEntity entity3 =
          new DeviceEntity(
              UUID.randomUUID(), "iPad Pro", "Apple", DeviceState.INACTIVE, creationTime);

      List<DeviceEntity> entities = Arrays.asList(deviceEntity, entity2, entity3);
      when(jpaRepository.findAll()).thenReturn(entities);

      List<Device> result = deviceRepositoryAdapter.findAll();

      assertNotNull(result);
      assertEquals(3, result.size());
      assertEquals("iPhone 14", result.get(0).getName());
      assertEquals("Samsung Galaxy", result.get(1).getName());
      assertEquals("iPad Pro", result.get(2).getName());
      verify(jpaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no devices exist")
    void shouldReturnEmptyListWhenNoDevicesExist() {
      when(jpaRepository.findAll()).thenReturn(Collections.emptyList());

      List<Device> result = deviceRepositoryAdapter.findAll();

      assertNotNull(result);
      assertTrue(result.isEmpty());
      verify(jpaRepository, times(1)).findAll();
    }
  }

  @Nested
  @DisplayName("Find By Brand Tests")
  class FindByBrandTests {

    @Test
    @DisplayName("Should find devices by brand and convert to domain models")
    void shouldFindDevicesByBrandAndConvertToDomain() {
      DeviceEntity entity2 =
          new DeviceEntity(
              UUID.randomUUID(), "iPad Pro", "Apple", DeviceState.INACTIVE, creationTime);

      List<DeviceEntity> appleEntities = Arrays.asList(deviceEntity, entity2);
      when(jpaRepository.findByBrand("Apple")).thenReturn(appleEntities);

      List<Device> result = deviceRepositoryAdapter.findByBrand("Apple");

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals("Apple", result.get(0).getBrand());
      assertEquals("Apple", result.get(1).getBrand());
      verify(jpaRepository, times(1)).findByBrand("Apple");
    }

    @Test
    @DisplayName("Should return empty list when no devices found for brand")
    void shouldReturnEmptyListWhenNoDevicesFoundForBrand() {
      when(jpaRepository.findByBrand("Dell")).thenReturn(Collections.emptyList());

      List<Device> result = deviceRepositoryAdapter.findByBrand("Dell");

      assertNotNull(result);
      assertTrue(result.isEmpty());
      verify(jpaRepository, times(1)).findByBrand("Dell");
    }
  }

  @Nested
  @DisplayName("Find By State Tests")
  class FindByStateTests {

    @Test
    @DisplayName("Should find devices by AVAILABLE state and convert to domain models")
    void shouldFindDevicesByAvailableStateAndConvertToDomain() {
      List<DeviceEntity> availableEntities = Collections.singletonList(deviceEntity);
      when(jpaRepository.findByState(DeviceState.AVAILABLE)).thenReturn(availableEntities);

      List<Device> result = deviceRepositoryAdapter.findByState(DeviceState.AVAILABLE);

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(DeviceState.AVAILABLE, result.get(0).getState());
      verify(jpaRepository, times(1)).findByState(DeviceState.AVAILABLE);
    }

    @Test
    @DisplayName("Should find devices by IN_USE state and convert to domain models")
    void shouldFindDevicesByInUseStateAndConvertToDomain() {
      DeviceEntity inUseEntity =
          new DeviceEntity(UUID.randomUUID(), "Laptop", "Dell", DeviceState.IN_USE, creationTime);
      List<DeviceEntity> inUseEntities = Collections.singletonList(inUseEntity);
      when(jpaRepository.findByState(DeviceState.IN_USE)).thenReturn(inUseEntities);

      List<Device> result = deviceRepositoryAdapter.findByState(DeviceState.IN_USE);

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(DeviceState.IN_USE, result.get(0).getState());
      verify(jpaRepository, times(1)).findByState(DeviceState.IN_USE);
    }

    @Test
    @DisplayName("Should find devices by INACTIVE state and convert to domain models")
    void shouldFindDevicesByInactiveStateAndConvertToDomain() {
      DeviceEntity inactiveEntity =
          new DeviceEntity(UUID.randomUUID(), "Monitor", "LG", DeviceState.INACTIVE, creationTime);
      List<DeviceEntity> inactiveEntities = Collections.singletonList(inactiveEntity);
      when(jpaRepository.findByState(DeviceState.INACTIVE)).thenReturn(inactiveEntities);

      List<Device> result = deviceRepositoryAdapter.findByState(DeviceState.INACTIVE);

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(DeviceState.INACTIVE, result.get(0).getState());
      verify(jpaRepository, times(1)).findByState(DeviceState.INACTIVE);
    }

    @Test
    @DisplayName("Should return empty list when no devices found for state")
    void shouldReturnEmptyListWhenNoDevicesFoundForState() {
      when(jpaRepository.findByState(DeviceState.IN_USE)).thenReturn(Collections.emptyList());

      List<Device> result = deviceRepositoryAdapter.findByState(DeviceState.IN_USE);

      assertNotNull(result);
      assertTrue(result.isEmpty());
      verify(jpaRepository, times(1)).findByState(DeviceState.IN_USE);
    }
  }

  @Nested
  @DisplayName("Delete By ID Tests")
  class DeleteByIdTests {

    @Test
    @DisplayName("Should delete device by ID")
    void shouldDeleteDeviceById() {
      doNothing().when(jpaRepository).deleteById(deviceId);

      deviceRepositoryAdapter.deleteById(deviceId);

      verify(jpaRepository, times(1)).deleteById(deviceId);
    }
  }

  @Nested
  @DisplayName("Exists By ID Tests")
  class ExistsByIdTests {

    @Test
    @DisplayName("Should return true when device exists")
    void shouldReturnTrueWhenDeviceExists() {
      when(jpaRepository.existsById(deviceId)).thenReturn(true);

      boolean result = deviceRepositoryAdapter.existsById(deviceId);

      assertTrue(result);
      verify(jpaRepository, times(1)).existsById(deviceId);
    }

    @Test
    @DisplayName("Should return false when device does not exist")
    void shouldReturnFalseWhenDeviceDoesNotExist() {
      UUID nonExistentId = UUID.randomUUID();
      when(jpaRepository.existsById(nonExistentId)).thenReturn(false);

      boolean result = deviceRepositoryAdapter.existsById(nonExistentId);

      assertFalse(result);
      verify(jpaRepository, times(1)).existsById(nonExistentId);
    }
  }
}
