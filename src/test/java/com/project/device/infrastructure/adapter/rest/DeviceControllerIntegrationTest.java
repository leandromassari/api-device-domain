package com.project.device.infrastructure.adapter.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.device.domain.model.DeviceState;
import com.project.device.infrastructure.adapter.rest.dto.DeviceRequest;
import com.project.device.infrastructure.adapter.rest.dto.DeviceUpdateRequest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("Device Controller Integration Tests")
class DeviceControllerIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.flyway.enabled", () -> "true");
  }

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired
  private com.project.device.infrastructure.adapter.persistence.DeviceJpaRepository
      deviceJpaRepository;

  @BeforeEach
  void setUp() {
    // Clean database before each test
    deviceJpaRepository.deleteAll();
  }

  @Nested
  @DisplayName("Create Device Tests (POST /api/v1/devices)")
  class CreateDeviceTests {

    @Test
    @DisplayName("Should create device and return 201 Created")
    void shouldCreateDeviceAndReturn201() throws Exception {
      DeviceRequest request = new DeviceRequest();
      request.setName("iPhone 14");
      request.setBrand("Apple");
      request.setState(DeviceState.AVAILABLE);

      mockMvc
          .perform(
              post("/api/v1/devices")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").exists())
          .andExpect(jsonPath("$.name").value("iPhone 14"))
          .andExpect(jsonPath("$.brand").value("Apple"))
          .andExpect(jsonPath("$.state").value("AVAILABLE"))
          .andExpect(jsonPath("$.creationTime").exists());
    }

    @Test
    @DisplayName("Should return 400 when name is null")
    void shouldReturn400WhenNameIsNull() throws Exception {
      DeviceRequest request = new DeviceRequest();
      request.setName(null);
      request.setBrand("Apple");
      request.setState(DeviceState.AVAILABLE);

      mockMvc
          .perform(
              post("/api/v1/devices")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when brand is null")
    void shouldReturn400WhenBrandIsNull() throws Exception {
      DeviceRequest request = new DeviceRequest();
      request.setName("iPhone 14");
      request.setBrand(null);
      request.setState(DeviceState.AVAILABLE);

      mockMvc
          .perform(
              post("/api/v1/devices")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when state is null")
    void shouldReturn400WhenStateIsNull() throws Exception {
      DeviceRequest request = new DeviceRequest();
      request.setName("iPhone 14");
      request.setBrand("Apple");
      request.setState(null);

      mockMvc
          .perform(
              post("/api/v1/devices")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 500 with invalid JSON (Spring default behavior)")
    void shouldReturn500WithInvalidJson() throws Exception {
      // Spring returns 500 for malformed JSON by default
      mockMvc
          .perform(
              post("/api/v1/devices")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{invalid json"))
          .andExpect(status().isInternalServerError());
    }
  }

  @Nested
  @DisplayName("Get Device Tests (GET /api/v1/devices/{id})")
  class GetDeviceByIdTests {

    @Test
    @DisplayName("Should get device by ID and return 200 OK")
    void shouldGetDeviceByIdAndReturn200() throws Exception {
      // Create device first
      DeviceRequest createRequest = new DeviceRequest();
      createRequest.setName("Samsung Galaxy");
      createRequest.setBrand("Samsung");
      createRequest.setState(DeviceState.AVAILABLE);

      MvcResult createResult =
          mockMvc
              .perform(
                  post("/api/v1/devices")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(createRequest)))
              .andReturn();

      String responseBody = createResult.getResponse().getContentAsString();
      String deviceId = objectMapper.readTree(responseBody).get("id").asText();

      // Get device by ID
      mockMvc
          .perform(get("/api/v1/devices/{id}", deviceId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(deviceId))
          .andExpect(jsonPath("$.name").value("Samsung Galaxy"))
          .andExpect(jsonPath("$.brand").value("Samsung"))
          .andExpect(jsonPath("$.state").value("AVAILABLE"));
    }

    @Test
    @DisplayName("Should return 404 when device not found")
    void shouldReturn404WhenDeviceNotFound() throws Exception {
      UUID nonExistentId = UUID.randomUUID();

      mockMvc
          .perform(get("/api/v1/devices/{id}", nonExistentId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Device not found with id: " + nonExistentId));
    }
  }

  @Nested
  @DisplayName("Get All Devices Tests (GET /api/v1/devices)")
  class GetAllDevicesTests {

    @Test
    @DisplayName("Should get all devices and return 200 OK")
    void shouldGetAllDevicesAndReturn200() throws Exception {
      // Create multiple devices
      createDevice("iPhone 14", "Apple", DeviceState.AVAILABLE);
      createDevice("Samsung Galaxy", "Samsung", DeviceState.IN_USE);

      mockMvc
          .perform(get("/api/v1/devices"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Should return empty list when no devices exist")
    void shouldReturnEmptyListWhenNoDevicesExist() throws Exception {
      mockMvc
          .perform(get("/api/v1/devices"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(0)));
    }
  }

  @Nested
  @DisplayName("Get Devices by Brand Tests (GET /api/v1/devices/brand/{brand})")
  class GetDevicesByBrandTests {

    @Test
    @DisplayName("Should get devices by brand and return 200 OK")
    void shouldGetDevicesByBrandAndReturn200() throws Exception {
      // Create devices with different brands
      createDevice("iPhone 14", "Apple", DeviceState.AVAILABLE);
      createDevice("iPad Pro", "Apple", DeviceState.INACTIVE);
      createDevice("Samsung Galaxy", "Samsung", DeviceState.IN_USE);

      mockMvc
          .perform(get("/api/v1/devices/brand/{brand}", "Apple"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(2)))
          .andExpect(jsonPath("$[0].brand").value("Apple"))
          .andExpect(jsonPath("$[1].brand").value("Apple"));
    }

    @Test
    @DisplayName("Should return empty list when no devices found for brand")
    void shouldReturnEmptyListWhenNoDevicesFoundForBrand() throws Exception {
      mockMvc
          .perform(get("/api/v1/devices/brand/{brand}", "Dell"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(0)));
    }
  }

  @Nested
  @DisplayName("Get Devices by State Tests (GET /api/v1/devices/state/{state})")
  class GetDevicesByStateTests {

    @Test
    @DisplayName("Should get devices by AVAILABLE state and return 200 OK")
    void shouldGetDevicesByAvailableStateAndReturn200() throws Exception {
      createDevice("iPhone 14", "Apple", DeviceState.AVAILABLE);
      createDevice("Samsung Galaxy", "Samsung", DeviceState.IN_USE);
      createDevice("Dell Laptop", "Dell", DeviceState.AVAILABLE);

      mockMvc
          .perform(get("/api/v1/devices/state/{state}", "AVAILABLE"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(2)))
          .andExpect(jsonPath("$[0].state").value("AVAILABLE"))
          .andExpect(jsonPath("$[1].state").value("AVAILABLE"));
    }

    @Test
    @DisplayName("Should get devices by IN_USE state and return 200 OK")
    void shouldGetDevicesByInUseStateAndReturn200() throws Exception {
      createDevice("iPhone 14", "Apple", DeviceState.IN_USE);
      createDevice("Samsung Galaxy", "Samsung", DeviceState.AVAILABLE);

      mockMvc
          .perform(get("/api/v1/devices/state/{state}", "IN_USE"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(1)))
          .andExpect(jsonPath("$[0].state").value("IN_USE"));
    }

    @Test
    @DisplayName("Should get devices by INACTIVE state and return 200 OK")
    void shouldGetDevicesByInactiveStateAndReturn200() throws Exception {
      createDevice("Old Device", "Legacy", DeviceState.INACTIVE);

      mockMvc
          .perform(get("/api/v1/devices/state/{state}", "INACTIVE"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(1)))
          .andExpect(jsonPath("$[0].state").value("INACTIVE"));
    }
  }

  @Nested
  @DisplayName("Full Update Device Tests (PUT /api/v1/devices/{id})")
  class FullUpdateDeviceTests {

    @Test
    @DisplayName("Should fully update device and return 200 OK")
    void shouldFullyUpdateDeviceAndReturn200() throws Exception {
      String deviceId = createDevice("iPhone 14", "Apple", DeviceState.AVAILABLE);

      DeviceRequest updateRequest = new DeviceRequest();
      updateRequest.setName("iPhone 15");
      updateRequest.setBrand("Apple");
      updateRequest.setState(DeviceState.IN_USE);

      mockMvc
          .perform(
              put("/api/v1/devices/{id}", deviceId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(deviceId))
          .andExpect(jsonPath("$.name").value("iPhone 15"))
          .andExpect(jsonPath("$.brand").value("Apple"))
          .andExpect(jsonPath("$.state").value("IN_USE"));
    }

    @Test
    @DisplayName("Should return 400 when trying to update name on IN_USE device")
    void shouldReturn400WhenUpdatingNameOnInUseDevice() throws Exception {
      String deviceId = createDevice("iPhone 14", "Apple", DeviceState.IN_USE);

      DeviceRequest updateRequest = new DeviceRequest();
      updateRequest.setName("iPhone 15");
      updateRequest.setBrand("Apple");
      updateRequest.setState(DeviceState.IN_USE);

      mockMvc
          .perform(
              put("/api/v1/devices/{id}", deviceId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message")
                  .value("Cannot update name or brand of device in IN_USE state: " + deviceId));
    }

    @Test
    @DisplayName("Should return 400 when trying to update brand on IN_USE device")
    void shouldReturn400WhenUpdatingBrandOnInUseDevice() throws Exception {
      String deviceId = createDevice("Samsung Galaxy", "Samsung", DeviceState.IN_USE);

      DeviceRequest updateRequest = new DeviceRequest();
      updateRequest.setName("Samsung Galaxy");
      updateRequest.setBrand("Google");
      updateRequest.setState(DeviceState.IN_USE);

      mockMvc
          .perform(
              put("/api/v1/devices/{id}", deviceId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message")
                  .value("Cannot update name or brand of device in IN_USE state: " + deviceId));
    }

    @Test
    @DisplayName("Should return 404 when device not found")
    void shouldReturn404WhenDeviceNotFound() throws Exception {
      UUID nonExistentId = UUID.randomUUID();
      DeviceRequest updateRequest = new DeviceRequest();
      updateRequest.setName("iPhone 15");
      updateRequest.setBrand("Apple");
      updateRequest.setState(DeviceState.AVAILABLE);

      mockMvc
          .perform(
              put("/api/v1/devices/{id}", nonExistentId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Partial Update Device Tests (PATCH /api/v1/devices/{id})")
  class PartialUpdateDeviceTests {

    @Test
    @DisplayName("Should partially update device name and return 200 OK")
    void shouldPartiallyUpdateDeviceNameAndReturn200() throws Exception {
      String deviceId = createDevice("iPhone 14", "Apple", DeviceState.AVAILABLE);

      DeviceUpdateRequest updateRequest = new DeviceUpdateRequest();
      updateRequest.setName("iPhone 15");

      mockMvc
          .perform(
              patch("/api/v1/devices/{id}", deviceId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(deviceId))
          .andExpect(jsonPath("$.name").value("iPhone 15"))
          .andExpect(jsonPath("$.brand").value("Apple"))
          .andExpect(jsonPath("$.state").value("AVAILABLE"));
    }

    @Test
    @DisplayName("Should partially update device state and return 200 OK")
    void shouldPartiallyUpdateDeviceStateAndReturn200() throws Exception {
      String deviceId = createDevice("Samsung Galaxy", "Samsung", DeviceState.AVAILABLE);

      DeviceUpdateRequest updateRequest = new DeviceUpdateRequest();
      updateRequest.setState(DeviceState.IN_USE);

      mockMvc
          .perform(
              patch("/api/v1/devices/{id}", deviceId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(deviceId))
          .andExpect(jsonPath("$.name").value("Samsung Galaxy"))
          .andExpect(jsonPath("$.brand").value("Samsung"))
          .andExpect(jsonPath("$.state").value("IN_USE"));
    }

    @Test
    @DisplayName("Should return 400 when trying to update name on IN_USE device")
    void shouldReturn400WhenUpdatingNameOnInUseDevice() throws Exception {
      String deviceId = createDevice("Laptop", "Dell", DeviceState.IN_USE);

      DeviceUpdateRequest updateRequest = new DeviceUpdateRequest();
      updateRequest.setName("New Laptop");

      mockMvc
          .perform(
              patch("/api/v1/devices/{id}", deviceId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message")
                  .value("Cannot update name or brand of device in IN_USE state: " + deviceId));
    }

    @Test
    @DisplayName("Should allow state update on IN_USE device without updating name/brand")
    void shouldAllowStateUpdateOnInUseDevice() throws Exception {
      String deviceId = createDevice("Monitor", "LG", DeviceState.IN_USE);

      DeviceUpdateRequest updateRequest = new DeviceUpdateRequest();
      updateRequest.setState(DeviceState.AVAILABLE);

      mockMvc
          .perform(
              patch("/api/v1/devices/{id}", deviceId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.state").value("AVAILABLE"));
    }
  }

  @Nested
  @DisplayName("Delete Device Tests (DELETE /api/v1/devices/{id})")
  class DeleteDeviceTests {

    @Test
    @DisplayName("Should delete device and return 204 No Content")
    void shouldDeleteDeviceAndReturn204() throws Exception {
      String deviceId = createDevice("Old Phone", "Nokia", DeviceState.AVAILABLE);

      mockMvc.perform(delete("/api/v1/devices/{id}", deviceId)).andExpect(status().isNoContent());

      // Verify device is deleted
      mockMvc.perform(get("/api/v1/devices/{id}", deviceId)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 409 Conflict when trying to delete IN_USE device")
    void shouldReturn409WhenDeletingInUseDevice() throws Exception {
      String deviceId = createDevice("Active Device", "Samsung", DeviceState.IN_USE);

      mockMvc
          .perform(delete("/api/v1/devices/{id}", deviceId))
          .andExpect(status().isConflict())
          .andExpect(
              jsonPath("$.message")
                  .value("Device is currently in use and cannot be deleted: " + deviceId));
    }

    @Test
    @DisplayName("Should return 404 when device not found")
    void shouldReturn404WhenDeviceNotFound() throws Exception {
      UUID nonExistentId = UUID.randomUUID();

      mockMvc
          .perform(delete("/api/v1/devices/{id}", nonExistentId))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should successfully delete INACTIVE device")
    void shouldSuccessfullyDeleteInactiveDevice() throws Exception {
      String deviceId = createDevice("Retired Device", "Legacy", DeviceState.INACTIVE);

      mockMvc.perform(delete("/api/v1/devices/{id}", deviceId)).andExpect(status().isNoContent());
    }
  }

  // Helper method to create a device and return its ID
  private String createDevice(String name, String brand, DeviceState state) throws Exception {
    DeviceRequest request = new DeviceRequest();
    request.setName(name);
    request.setBrand(brand);
    request.setState(state);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/devices")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    return objectMapper.readTree(responseBody).get("id").asText();
  }
}
