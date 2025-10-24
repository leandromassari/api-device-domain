package com.project.device.infrastructure.adapter.rest;

import com.project.device.domain.exception.DeviceInUseException;
import com.project.device.domain.exception.DeviceNotFoundException;
import com.project.device.domain.exception.InvalidDeviceOperationException;
import com.project.device.infrastructure.adapter.rest.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for REST API.
 *
 * <p>Provides centralized exception handling across all controllers, converting domain exceptions
 * and validation errors into appropriate HTTP responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles DeviceNotFoundException.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return 404 Not Found response
   */
  @ExceptionHandler(DeviceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleDeviceNotFound(
      DeviceNotFoundException ex, HttpServletRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  /**
   * Handles DeviceInUseException.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return 409 Conflict response
   */
  @ExceptionHandler(DeviceInUseException.class)
  public ResponseEntity<ErrorResponse> handleDeviceInUse(
      DeviceInUseException ex, HttpServletRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  /**
   * Handles InvalidDeviceOperationException.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return 400 Bad Request response
   */
  @ExceptionHandler(InvalidDeviceOperationException.class)
  public ResponseEntity<ErrorResponse> handleInvalidDeviceOperation(
      InvalidDeviceOperationException ex, HttpServletRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handles IllegalArgumentException.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return 400 Bad Request response
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handles Bean Validation errors.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return 400 Bad Request response with validation details
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationErrors(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<String> details =
        ex.getBindingResult().getAllErrors().stream()
            .map(
                error -> {
                  if (error instanceof FieldError fieldError) {
                    return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                  }
                  return error.getDefaultMessage();
                })
            .toList();

    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "Invalid request data",
            request.getRequestURI(),
            details);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handles all other unexpected exceptions.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return 500 Internal Server Error response
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex, HttpServletRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred",
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
