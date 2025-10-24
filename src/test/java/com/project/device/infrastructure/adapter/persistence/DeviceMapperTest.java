package com.project.device.infrastructure.adapter.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.project.device.domain.model.Device;
import com.project.device.domain.model.DeviceState;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DeviceMapper Tests")
class DeviceMapperTest {

  private UUID deviceId;
  private LocalDateTime creationTime;

  @BeforeEach
  void setUp() {
    deviceId = UUID.randomUUID();
    creationTime = LocalDateTime.now();
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should throw exception when trying to instantiate utility class")
    void shouldThrowExceptionWhenInstantiating() throws Exception {
      var constructor = DeviceMapper.class.getDeclaredConstructor();
      constructor.setAccessible(true);

      var exception =
          assertThrows(
              java.lang.reflect.InvocationTargetException.class, () -> constructor.newInstance());

      assertEquals(UnsupportedOperationException.class, exception.getCause().getClass());
      assertEquals("Utility class cannot be instantiated", exception.getCause().getMessage());
    }
  }

  @Nested
  @DisplayName("Domain to Entity Mapping Tests")
  class DomainToEntityMappingTests {

    @Test
    @DisplayName("Should convert domain model to entity with all fields")
    void shouldConvertDomainToEntityWithAllFields() {
      Device domain =
          new Device(deviceId, "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);

      DeviceEntity entity = DeviceMapper.toEntity(domain);

      assertNotNull(entity);
      assertEquals(deviceId, entity.getId());
      assertEquals("iPhone 14", entity.getName());
      assertEquals("Apple", entity.getBrand());
      assertEquals(DeviceState.AVAILABLE, entity.getState());
      assertEquals(creationTime, entity.getCreationTime());
    }

    @Test
    @DisplayName("Should convert domain model with IN_USE state to entity")
    void shouldConvertDomainWithInUseStateToEntity() {
      Device domain =
          new Device(deviceId, "Samsung Galaxy", "Samsung", DeviceState.IN_USE, creationTime);

      DeviceEntity entity = DeviceMapper.toEntity(domain);

      assertNotNull(entity);
      assertEquals(DeviceState.IN_USE, entity.getState());
    }

    @Test
    @DisplayName("Should convert domain model with INACTIVE state to entity")
    void shouldConvertDomainWithInactiveStateToEntity() {
      Device domain = new Device(deviceId, "iPad Pro", "Apple", DeviceState.INACTIVE, creationTime);

      DeviceEntity entity = DeviceMapper.toEntity(domain);

      assertNotNull(entity);
      assertEquals(DeviceState.INACTIVE, entity.getState());
    }

    @Test
    @DisplayName("Should return null when converting null domain model to entity")
    void shouldReturnNullWhenConvertingNullDomainToEntity() {
      DeviceEntity entity = DeviceMapper.toEntity(null);

      assertNull(entity);
    }
  }

  @Nested
  @DisplayName("Entity to Domain Mapping Tests")
  class EntityToDomainMappingTests {

    @Test
    @DisplayName("Should convert entity to domain model with all fields")
    void shouldConvertEntityToDomainWithAllFields() {
      DeviceEntity entity =
          new DeviceEntity(deviceId, "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);

      Device domain = DeviceMapper.toDomain(entity);

      assertNotNull(domain);
      assertEquals(deviceId, domain.getId());
      assertEquals("iPhone 14", domain.getName());
      assertEquals("Apple", domain.getBrand());
      assertEquals(DeviceState.AVAILABLE, domain.getState());
      assertEquals(creationTime, domain.getCreationTime());
    }

    @Test
    @DisplayName("Should convert entity with IN_USE state to domain model")
    void shouldConvertEntityWithInUseStateToDomain() {
      DeviceEntity entity =
          new DeviceEntity(deviceId, "Laptop", "Dell", DeviceState.IN_USE, creationTime);

      Device domain = DeviceMapper.toDomain(entity);

      assertNotNull(domain);
      assertEquals(DeviceState.IN_USE, domain.getState());
    }

    @Test
    @DisplayName("Should convert entity with INACTIVE state to domain model")
    void shouldConvertEntityWithInactiveStateToDomain() {
      DeviceEntity entity =
          new DeviceEntity(deviceId, "Monitor", "LG", DeviceState.INACTIVE, creationTime);

      Device domain = DeviceMapper.toDomain(entity);

      assertNotNull(domain);
      assertEquals(DeviceState.INACTIVE, domain.getState());
    }

    @Test
    @DisplayName("Should return null when converting null entity to domain model")
    void shouldReturnNullWhenConvertingNullEntityToDomain() {
      Device domain = DeviceMapper.toDomain(null);

      assertNull(domain);
    }
  }

  @Nested
  @DisplayName("Bidirectional Mapping Tests")
  class BidirectionalMappingTests {

    @Test
    @DisplayName("Should maintain data integrity when converting domain to entity and back")
    void shouldMaintainDataIntegrityWhenConvertingDomainToEntityAndBack() {
      Device originalDomain =
          new Device(deviceId, "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);

      DeviceEntity entity = DeviceMapper.toEntity(originalDomain);
      Device resultDomain = DeviceMapper.toDomain(entity);

      assertNotNull(resultDomain);
      assertEquals(originalDomain.getId(), resultDomain.getId());
      assertEquals(originalDomain.getName(), resultDomain.getName());
      assertEquals(originalDomain.getBrand(), resultDomain.getBrand());
      assertEquals(originalDomain.getState(), resultDomain.getState());
      assertEquals(originalDomain.getCreationTime(), resultDomain.getCreationTime());
    }

    @Test
    @DisplayName("Should maintain data integrity when converting entity to domain and back")
    void shouldMaintainDataIntegrityWhenConvertingEntityToDomainAndBack() {
      DeviceEntity originalEntity =
          new DeviceEntity(deviceId, "Samsung Galaxy", "Samsung", DeviceState.IN_USE, creationTime);

      Device domain = DeviceMapper.toDomain(originalEntity);
      DeviceEntity resultEntity = DeviceMapper.toEntity(domain);

      assertNotNull(resultEntity);
      assertEquals(originalEntity.getId(), resultEntity.getId());
      assertEquals(originalEntity.getName(), resultEntity.getName());
      assertEquals(originalEntity.getBrand(), resultEntity.getBrand());
      assertEquals(originalEntity.getState(), resultEntity.getState());
      assertEquals(originalEntity.getCreationTime(), resultEntity.getCreationTime());
    }
  }
}
