package com.project.device.infrastructure.adapter.rest.dto;

import com.project.device.domain.model.DeviceState;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing a device.
 *
 * <p>Contains all device properties to be returned in API responses.
 */
public class DeviceResponse {

  private UUID id;
  private String name;
  private String brand;
  private DeviceState state;
  private LocalDateTime creationTime;

  public DeviceResponse() {}

  public DeviceResponse(
      UUID id, String name, String brand, DeviceState state, LocalDateTime creationTime) {
    this.id = id;
    this.name = name;
    this.brand = brand;
    this.state = state;
    this.creationTime = creationTime;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
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

  public LocalDateTime getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(LocalDateTime creationTime) {
    this.creationTime = creationTime;
  }
}
