package com.project.device.domain.exception;

public class InvalidDeviceOperationException extends RuntimeException {

  public InvalidDeviceOperationException(String message) {
    super(message);
  }

  public InvalidDeviceOperationException(String message, Throwable cause) {
    super(message, cause);
  }
}
