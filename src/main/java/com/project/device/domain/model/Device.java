package com.project.device.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Device {

  private UUID id;
  private String name;
  private String brand;
  private DeviceState state;
  private LocalDateTime creationTime;

  public Device(UUID id, String name, String brand, DeviceState state, LocalDateTime creationTime) {
    this.id = id;
    this.name = name;
    this.brand = brand;
    this.state = state;
    this.creationTime = creationTime;
  }

  public Device() {}

  public boolean canDelete() {
    return state != DeviceState.IN_USE;
  }

  public boolean canUpdateNameOrBrand() {
    return state == DeviceState.AVAILABLE || state == DeviceState.INACTIVE;
  }

  public boolean validateStateTransition(DeviceState newState) {
    if (this.state == newState) {
      return true;
    }

    return switch (this.state) {
      case AVAILABLE -> newState == DeviceState.IN_USE || newState == DeviceState.INACTIVE;
      case IN_USE -> newState == DeviceState.AVAILABLE || newState == DeviceState.INACTIVE;
      case INACTIVE -> newState == DeviceState.AVAILABLE;
    };
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Device device = (Device) o;
    return Objects.equals(id, device.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Device{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", brand='"
        + brand
        + '\''
        + ", state="
        + state
        + ", creationTime="
        + creationTime
        + '}';
  }
}
