package com.project.device.infrastructure.adapter.persistence;

import com.project.device.domain.model.DeviceState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing a device in the database.
 *
 * <p>This entity maps to the "devices" table and serves as the persistence layer representation of
 * the Device domain model.
 */
@Entity
@Table(name = "devices")
public class DeviceEntity {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "brand", nullable = false, length = 255)
  private String brand;

  @Enumerated(EnumType.STRING)
  @Column(name = "state", nullable = false, length = 20)
  private DeviceState state;

  @Column(name = "creation_time", updatable = false, nullable = false)
  private LocalDateTime creationTime;

  public DeviceEntity() {}

  public DeviceEntity(
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
