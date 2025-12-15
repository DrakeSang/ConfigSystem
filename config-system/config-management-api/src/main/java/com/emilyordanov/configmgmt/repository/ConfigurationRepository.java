package com.emilyordanov.configmgmt.repository;

import com.emilyordanov.configmgmt.entity.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConfigurationRepository extends JpaRepository<Configuration, UUID> {
    Optional<Configuration> findTopByAppNameAndEnvOrderByVersionDesc(String appName, String env);

    Optional<Configuration> findTopByAppNameAndEnvAndDeletedAtIsNullOrderByVersionDesc(String appName, String env);

    Optional<Configuration> findByIdAndDeletedAtIsNull(UUID id);

    List<Configuration> findAllByAppNameAndEnvAndDeletedAtIsNull(String appName, String env);
}
