package com.project.device.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Device Domain Model Tests")
class DeviceTest {

  private UUID deviceId;
  private LocalDateTime creationTime;

  @BeforeEach
  void setUp() {
    deviceId = UUID.randomUUID();
    creationTime = LocalDateTime.now();
  }

  @Nested
  @DisplayName("Device Creation Tests")
  class DeviceCreationTests {

    @Test
    @DisplayName("Should create device with all fields using constructor")
    void shouldCreateDeviceWithConstructor() {
      Device device =
          new Device(deviceId, "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);

      assertEquals(deviceId, device.getId());
      assertEquals("iPhone 14", device.getName());
      assertEquals("Apple", device.getBrand());
      assertEquals(DeviceState.AVAILABLE, device.getState());
      assertEquals(creationTime, device.getCreationTime());
    }

    @Test
    @DisplayName("Should create device with no-args constructor")
    void shouldCreateDeviceWithNoArgsConstructor() {
      Device device = new Device();
      device.setId(deviceId);
      device.setName("Samsung Galaxy");
      device.setBrand("Samsung");
      device.setState(DeviceState.IN_USE);
      device.setCreationTime(creationTime);

      assertEquals(deviceId, device.getId());
      assertEquals("Samsung Galaxy", device.getName());
      assertEquals("Samsung", device.getBrand());
      assertEquals(DeviceState.IN_USE, device.getState());
      assertEquals(creationTime, device.getCreationTime());
    }
  }

  @Nested
  @DisplayName("canDelete() Business Logic Tests")
  class CanDeleteTests {

    @Test
    @DisplayName("Should allow deletion when device is AVAILABLE")
    void shouldAllowDeletionWhenAvailable() {
      Device device =
          new Device(deviceId, "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);

      assertTrue(device.canDelete(), "Device in AVAILABLE state should be deletable");
    }

    @Test
    @DisplayName("Should allow deletion when device is INACTIVE")
    void shouldAllowDeletionWhenInactive() {
      Device device =
          new Device(deviceId, "iPhone 14", "Apple", DeviceState.INACTIVE, creationTime);

      assertTrue(device.canDelete(), "Device in INACTIVE state should be deletable");
    }

    @Test
    @DisplayName("Should NOT allow deletion when device is IN_USE")
    void shouldNotAllowDeletionWhenInUse() {
      Device device = new Device(deviceId, "iPhone 14", "Apple", DeviceState.IN_USE, creationTime);

      assertFalse(device.canDelete(), "Device in IN_USE state should NOT be deletable");
    }
  }

  @Nested
  @DisplayName("canUpdateNameOrBrand() Business Logic Tests")
  class CanUpdateNameOrBrandTests {

    @Test
    @DisplayName("Should allow name/brand update when device is AVAILABLE")
    void shouldAllowUpdateWhenAvailable() {
      Device device =
          new Device(deviceId, "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);

      assertTrue(
          device.canUpdateNameOrBrand(),
          "Device in AVAILABLE state should allow name/brand updates");
    }

    @Test
    @DisplayName("Should allow name/brand update when device is INACTIVE")
    void shouldAllowUpdateWhenInactive() {
      Device device =
          new Device(deviceId, "iPhone 14", "Apple", DeviceState.INACTIVE, creationTime);

      assertTrue(
          device.canUpdateNameOrBrand(),
          "Device in INACTIVE state should allow name/brand updates");
    }

    @Test
    @DisplayName("Should NOT allow name/brand update when device is IN_USE")
    void shouldNotAllowUpdateWhenInUse() {
      Device device = new Device(deviceId, "iPhone 14", "Apple", DeviceState.IN_USE, creationTime);

      assertFalse(
          device.canUpdateNameOrBrand(),
          "Device in IN_USE state should NOT allow name/brand updates");
    }
  }

  @Nested
  @DisplayName("State Transition Validation Tests")
  class StateTransitionTests {

    @Nested
    @DisplayName("From AVAILABLE State Transitions")
    class FromAvailableTransitions {

      private Device device;

      @BeforeEach
      void setUp() {
        device = new Device(deviceId, "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);
      }

      @Test
      @DisplayName("Should allow AVAILABLE -> AVAILABLE (same state)")
      void shouldAllowAvailableToAvailable() {
        assertTrue(device.validateStateTransition(DeviceState.AVAILABLE));
      }

      @Test
      @DisplayName("Should allow AVAILABLE -> IN_USE")
      void shouldAllowAvailableToInUse() {
        assertTrue(device.validateStateTransition(DeviceState.IN_USE));
      }

      @Test
      @DisplayName("Should allow AVAILABLE -> INACTIVE")
      void shouldAllowAvailableToInactive() {
        assertTrue(device.validateStateTransition(DeviceState.INACTIVE));
      }
    }

    @Nested
    @DisplayName("From IN_USE State Transitions")
    class FromInUseTransitions {

      private Device device;

      @BeforeEach
      void setUp() {
        device = new Device(deviceId, "iPhone 14", "Apple", DeviceState.IN_USE, creationTime);
      }

      @Test
      @DisplayName("Should allow IN_USE -> IN_USE (same state)")
      void shouldAllowInUseToInUse() {
        assertTrue(device.validateStateTransition(DeviceState.IN_USE));
      }

      @Test
      @DisplayName("Should allow IN_USE -> AVAILABLE")
      void shouldAllowInUseToAvailable() {
        assertTrue(device.validateStateTransition(DeviceState.AVAILABLE));
      }

      @Test
      @DisplayName("Should allow IN_USE -> INACTIVE")
      void shouldAllowInUseToInactive() {
        assertTrue(device.validateStateTransition(DeviceState.INACTIVE));
      }
    }

    @Nested
    @DisplayName("From INACTIVE State Transitions")
    class FromInactiveTransitions {

      private Device device;

      @BeforeEach
      void setUp() {
        device = new Device(deviceId, "iPhone 14", "Apple", DeviceState.INACTIVE, creationTime);
      }

      @Test
      @DisplayName("Should allow INACTIVE -> INACTIVE (same state)")
      void shouldAllowInactiveToInactive() {
        assertTrue(device.validateStateTransition(DeviceState.INACTIVE));
      }

      @Test
      @DisplayName("Should allow INACTIVE -> AVAILABLE")
      void shouldAllowInactiveToAvailable() {
        assertTrue(device.validateStateTransition(DeviceState.AVAILABLE));
      }

      @Test
      @DisplayName("Should NOT allow INACTIVE -> IN_USE")
      void shouldNotAllowInactiveToInUse() {
        assertFalse(
            device.validateStateTransition(DeviceState.IN_USE),
            "INACTIVE device cannot transition directly to IN_USE");
      }
    }
  }

  @Nested
  @DisplayName("Equals and HashCode Tests")
  class EqualsAndHashCodeTests {

    @Test
    @DisplayName("Should be equal when IDs are the same")
    void shouldBeEqualWhenIdsMatch() {
      Device device1 =
          new Device(deviceId, "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);
      Device device2 =
          new Device(deviceId, "Samsung Galaxy", "Samsung", DeviceState.IN_USE, creationTime);

      assertEquals(device1, device2, "Devices with same ID should be equal");
      assertEquals(
          device1.hashCode(), device2.hashCode(), "Devices with same ID should have same hashcode");
    }

    @Test
    @DisplayName("Should NOT be equal when IDs are different")
    void shouldNotBeEqualWhenIdsDiffer() {
      Device device1 =
          new Device(deviceId, "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);
      Device device2 =
          new Device(UUID.randomUUID(), "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);

      assertNotEquals(device1, device2, "Devices with different IDs should not be equal");
    }

    @Test
    @DisplayName("Should be equal to itself")
    void shouldBeEqualToItself() {
      Device device =
          new Device(deviceId, "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);

      assertEquals(device, device, "Device should be equal to itself");
    }

    @Test
    @DisplayName("Should NOT be equal to null")
    void shouldNotBeEqualToNull() {
      Device device =
          new Device(deviceId, "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);

      assertNotEquals(null, device, "Device should not be equal to null");
    }

    @Test
    @DisplayName("Should NOT be equal to different class")
    void shouldNotBeEqualToDifferentClass() {
      Device device =
          new Device(deviceId, "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);

      assertNotEquals(device, "not a device", "Device should not be equal to different class");
    }
  }

  @Nested
  @DisplayName("ToString Tests")
  class ToStringTests {

    @Test
    @DisplayName("Should contain all device information in toString")
    void shouldContainAllInformation() {
      Device device =
          new Device(deviceId, "iPhone 14", "Apple", DeviceState.AVAILABLE, creationTime);

      String toString = device.toString();

      assertTrue(toString.contains(deviceId.toString()), "toString should contain device ID");
      assertTrue(toString.contains("iPhone 14"), "toString should contain device name");
      assertTrue(toString.contains("Apple"), "toString should contain device brand");
      assertTrue(toString.contains("AVAILABLE"), "toString should contain device state");
      assertTrue(
          toString.contains(creationTime.toString()), "toString should contain creation time");
    }
  }

  @Nested
  @DisplayName("Getter and Setter Tests")
  class GetterSetterTests {

    @Test
    @DisplayName("Should get and set ID correctly")
    void shouldGetAndSetId() {
      Device device = new Device();
      UUID newId = UUID.randomUUID();

      device.setId(newId);

      assertEquals(newId, device.getId());
    }

    @Test
    @DisplayName("Should get and set name correctly")
    void shouldGetAndSetName() {
      Device device = new Device();

      device.setName("Test Device");

      assertEquals("Test Device", device.getName());
    }

    @Test
    @DisplayName("Should get and set brand correctly")
    void shouldGetAndSetBrand() {
      Device device = new Device();

      device.setBrand("Test Brand");

      assertEquals("Test Brand", device.getBrand());
    }

    @Test
    @DisplayName("Should get and set state correctly")
    void shouldGetAndSetState() {
      Device device = new Device();

      device.setState(DeviceState.IN_USE);

      assertEquals(DeviceState.IN_USE, device.getState());
    }

    @Test
    @DisplayName("Should get and set creation time correctly")
    void shouldGetAndSetCreationTime() {
      Device device = new Device();
      LocalDateTime time = LocalDateTime.now();

      device.setCreationTime(time);

      assertEquals(time, device.getCreationTime());
    }
  }
}
