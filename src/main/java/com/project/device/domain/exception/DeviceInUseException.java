package com.project.device.domain.exception;

import java.util.UUID;

public class DeviceInUseException extends RuntimeException {

  public DeviceInUseException(UUID id) {
    super("Device is currently in use and cannot be deleted: " + id);
  }

  public DeviceInUseException(String message) {
    super(message);
  }
}
