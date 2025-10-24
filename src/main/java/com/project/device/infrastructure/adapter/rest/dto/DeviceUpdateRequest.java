package com.project.device.infrastructure.adapter.rest.dto;

import com.project.device.domain.model.DeviceState;

/**
 * Request DTO for updating a device.
 *
 * <p>All fields are optional to support partial updates. Null values indicate the field should not
 * be updated.
 */
public class DeviceUpdateRequest {

  private String name;
  private String brand;
  private DeviceState state;

  public DeviceUpdateRequest() {}

  public DeviceUpdateRequest(String name, String brand, DeviceState state) {
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
