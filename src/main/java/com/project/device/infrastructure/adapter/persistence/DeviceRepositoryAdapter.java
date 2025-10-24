package com.project.device.infrastructure.adapter.persistence;

import com.project.device.domain.model.Device;
import com.project.device.domain.model.DeviceState;
import com.project.device.domain.port.DeviceRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Adapter implementation of DeviceRepositoryPort using Spring Data JPA.
 *
 * <p>This adapter bridges the domain layer with the persistence layer, converting between domain
 * models and JPA entities.
 *
 * <p>Implements the Hexagonal Architecture pattern by adapting external persistence technology to
 * the domain's port interface.
 */
@Component
public class DeviceRepositoryAdapter implements DeviceRepositoryPort {

  private final DeviceJpaRepository jpaRepository;

  /**
   * Constructs a new DeviceRepositoryAdapter with the required JPA repository.
   *
   * @param jpaRepository the Spring Data JPA repository
   */
  public DeviceRepositoryAdapter(DeviceJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Device save(Device device) {
    DeviceEntity entity = DeviceMapper.toEntity(device);
    DeviceEntity savedEntity = jpaRepository.save(entity);
    return DeviceMapper.toDomain(savedEntity);
  }

  @Override
  public Optional<Device> findById(UUID id) {
    return jpaRepository.findById(id).map(DeviceMapper::toDomain);
  }

  @Override
  public List<Device> findAll() {
    return jpaRepository.findAll().stream().map(DeviceMapper::toDomain).toList();
  }

  @Override
  public List<Device> findByBrand(String brand) {
    return jpaRepository.findByBrand(brand).stream().map(DeviceMapper::toDomain).toList();
  }

  @Override
  public List<Device> findByState(DeviceState state) {
    return jpaRepository.findByState(state).stream().map(DeviceMapper::toDomain).toList();
  }

  @Override
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public boolean existsById(UUID id) {
    return jpaRepository.existsById(id);
  }
}
