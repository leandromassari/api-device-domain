package com.project.device.infrastructure.adapter.rest;

import com.project.device.application.usecase.CreateDeviceUseCase;
import com.project.device.application.usecase.DeleteDeviceUseCase;
import com.project.device.application.usecase.GetDeviceUseCase;
import com.project.device.application.usecase.UpdateDeviceUseCase;
import com.project.device.domain.model.Device;
import com.project.device.domain.model.DeviceState;
import com.project.device.infrastructure.adapter.rest.dto.DeviceRequest;
import com.project.device.infrastructure.adapter.rest.dto.DeviceResponse;
import com.project.device.infrastructure.adapter.rest.dto.DeviceUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for device management operations.
 *
 * <p>Provides HTTP endpoints for creating, reading, updating, and deleting devices.
 */
@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

  private final CreateDeviceUseCase createDeviceUseCase;
  private final UpdateDeviceUseCase updateDeviceUseCase;
  private final GetDeviceUseCase getDeviceUseCase;
  private final DeleteDeviceUseCase deleteDeviceUseCase;

  /**
   * Constructs a new DeviceController with required use cases.
   *
   * @param createDeviceUseCase use case for creating devices
   * @param updateDeviceUseCase use case for updating devices
   * @param getDeviceUseCase use case for retrieving devices
   * @param deleteDeviceUseCase use case for deleting devices
   */
  public DeviceController(
      CreateDeviceUseCase createDeviceUseCase,
      UpdateDeviceUseCase updateDeviceUseCase,
      GetDeviceUseCase getDeviceUseCase,
      DeleteDeviceUseCase deleteDeviceUseCase) {
    this.createDeviceUseCase = createDeviceUseCase;
    this.updateDeviceUseCase = updateDeviceUseCase;
    this.getDeviceUseCase = getDeviceUseCase;
    this.deleteDeviceUseCase = deleteDeviceUseCase;
  }

  /**
   * Creates a new device.
   *
   * @param request the device creation request
   * @return 201 Created with the created device
   */
  @PostMapping
  public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody DeviceRequest request) {
    Device device =
        createDeviceUseCase.execute(request.getName(), request.getBrand(), request.getState());
    return ResponseEntity.status(HttpStatus.CREATED).body(DeviceMapper.toResponse(device));
  }

  /**
   * Performs a full update of a device.
   *
   * @param id the device ID
   * @param request the update request with all fields
   * @return 200 OK with the updated device
   */
  @PutMapping("/{id}")
  public ResponseEntity<DeviceResponse> fullUpdateDevice(
      @PathVariable UUID id, @Valid @RequestBody DeviceRequest request) {
    Device device =
        updateDeviceUseCase.fullUpdate(
            id, request.getName(), request.getBrand(), request.getState());
    return ResponseEntity.ok(DeviceMapper.toResponse(device));
  }

  /**
   * Performs a partial update of a device.
   *
   * @param id the device ID
   * @param request the update request with optional fields
   * @return 200 OK with the updated device
   */
  @PatchMapping("/{id}")
  public ResponseEntity<DeviceResponse> partialUpdateDevice(
      @PathVariable UUID id, @RequestBody DeviceUpdateRequest request) {
    Device device =
        updateDeviceUseCase.partialUpdate(
            id, request.getName(), request.getBrand(), request.getState());
    return ResponseEntity.ok(DeviceMapper.toResponse(device));
  }

  /**
   * Retrieves a device by ID.
   *
   * @param id the device ID
   * @return 200 OK with the device
   */
  @GetMapping("/{id}")
  public ResponseEntity<DeviceResponse> getDeviceById(@PathVariable UUID id) {
    Device device = getDeviceUseCase.findById(id);
    return ResponseEntity.ok(DeviceMapper.toResponse(device));
  }

  /**
   * Retrieves all devices.
   *
   * @return 200 OK with list of all devices
   */
  @GetMapping
  public ResponseEntity<List<DeviceResponse>> getAllDevices() {
    List<Device> devices = getDeviceUseCase.findAll();
    List<DeviceResponse> response =
        devices.stream().map(DeviceMapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(response);
  }

  /**
   * Retrieves devices by brand.
   *
   * @param brand the brand to filter by
   * @return 200 OK with list of matching devices
   */
  @GetMapping("/brand/{brand}")
  public ResponseEntity<List<DeviceResponse>> getDevicesByBrand(@PathVariable String brand) {
    List<Device> devices = getDeviceUseCase.findByBrand(brand);
    List<DeviceResponse> response =
        devices.stream().map(DeviceMapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(response);
  }

  /**
   * Retrieves devices by state.
   *
   * @param state the state to filter by
   * @return 200 OK with list of matching devices
   */
  @GetMapping("/state/{state}")
  public ResponseEntity<List<DeviceResponse>> getDevicesByState(@PathVariable DeviceState state) {
    List<Device> devices = getDeviceUseCase.findByState(state);
    List<DeviceResponse> response =
        devices.stream().map(DeviceMapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(response);
  }

  /**
   * Deletes a device by ID.
   *
   * @param id the device ID
   * @return 204 No Content
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDevice(@PathVariable UUID id) {
    deleteDeviceUseCase.execute(id);
    return ResponseEntity.noContent().build();
  }
}
