package com.project.device.domain.exception;

import java.util.UUID;

public class DeviceNotFoundException extends RuntimeException {

  public DeviceNotFoundException(UUID id) {
    super("Device not found with id: " + id);
  }

  public DeviceNotFoundException(String message) {
    super(message);
  }
}
