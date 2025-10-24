package com.project.device.infrastructure.adapter.rest.dto;

import com.project.device.domain.model.DeviceState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a new device.
 *
 * <p>Includes Bean Validation annotations to ensure data integrity.
 */
public class DeviceRequest {

  @NotBlank(message = "Device name is required and cannot be blank")
  private String name;

  @NotBlank(message = "Device brand is required and cannot be blank")
  private String brand;

  @NotNull(message = "Device state is required")
  private DeviceState state;

  public DeviceRequest() {}

  public DeviceRequest(String name, String brand, DeviceState state) {
    this.name = name;
    this.brand = brand;
    this.state = state;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public DeviceState getState() {
    return state;
  }

  public void setState(DeviceState state) {
    this.state = state;
  }
}
