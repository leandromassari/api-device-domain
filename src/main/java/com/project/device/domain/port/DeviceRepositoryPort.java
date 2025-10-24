package com.project.device.domain.port;

import com.project.device.domain.model.Device;
import com.project.device.domain.model.DeviceState;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepositoryPort {

  Device save(Device device);

  Optional<Device> findById(UUID id);

  List<Device> findAll();

  List<Device> findByBrand(String brand);

  List<Device> findByState(DeviceState state);

  void deleteById(UUID id);

  boolean existsById(UUID id);
}
