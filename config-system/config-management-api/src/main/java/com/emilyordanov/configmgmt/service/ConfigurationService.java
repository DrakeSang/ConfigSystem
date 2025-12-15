package com.emilyordanov.configmgmt.service;

import com.emilyordanov.configmgmt.cache.RedisKeys;
import com.emilyordanov.configmgmt.controller.exception.ConfigurationNotFoundException;
import com.emilyordanov.configmgmt.dto.CreateConfigurationRequest;
import com.emilyordanov.configmgmt.entity.Configuration;
import com.emilyordanov.configmgmt.event.ConfigurationChangeEvent;
import com.emilyordanov.configmgmt.kafka.ConfigurationEventProducer;
import com.emilyordanov.configmgmt.repository.ConfigurationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ConfigurationService {
    private final ConfigurationRepository configurationRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;

    private final ConfigurationEventProducer eventProducer;

    public ConfigurationService(ConfigurationRepository configurationRepository, RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper, ConfigurationEventProducer eventProducer) {
        this.configurationRepository = configurationRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.eventProducer = eventProducer;
    }

    public Configuration create(CreateConfigurationRequest request) {
        int nextVersion = configurationRepository
                .findTopByAppNameAndEnvOrderByVersionDesc(
                        request.getAppName(), request.getEnv())
                .map(c -> c.getVersion() + 1)
                .orElse(1);

        Configuration config = new Configuration();
        config.setId(UUID.randomUUID());
        config.setAppName(request.getAppName());
        config.setEnv(request.getEnv());
        config.setVersion(nextVersion);
        config.setData(request.getData());
        config.setCreatedAt(Instant.now());
        config.setUpdatedAt(Instant.now());

        Configuration saved = configurationRepository.save(config);

        // invalidate cache
        String key = RedisKeys.latestConfig(saved.getAppName(), saved.getEnv());
        redisTemplate.delete(key);

        eventProducer.publish(new ConfigurationChangeEvent(
                        "CONFIG_CREATED",
                        saved.getAppName(),
                        saved.getEnv(),
                        saved.getVersion(),
                        Instant.now()
                )
        );

        return saved;
    }

    public Configuration getLatest(String appName, String env) {
        String key = RedisKeys.latestConfig(appName, env);

        // 1. Try cache
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            // Convert LinkedHashMap → Configuration
            return objectMapper.convertValue(cached, Configuration.class);
        }

        // 2. Cache miss → DB
        Configuration config = configurationRepository
                .findTopByAppNameAndEnvAndDeletedAtIsNullOrderByVersionDesc(appName, env)
                .orElseThrow(() ->
                        new ConfigurationNotFoundException("Configuration not found"));

        // 3. Store in cache
        redisTemplate.opsForValue().set(key, config);

        return config;
    }

    public Configuration getById(UUID id) {
        return configurationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new ConfigurationNotFoundException("Configuration not found"));
    }

    public List<Configuration> list(String appName, String env) {
        return configurationRepository.findAllByAppNameAndEnvAndDeletedAtIsNull(appName, env);
    }

    // UPDATE = create new version
    public Configuration update(UUID id, JsonNode newData) {
        // 1. Load existing configuration (throws 404 if not found or deleted)
        Configuration existing = getById(id);

        // 2. Build a create request for the new version
        CreateConfigurationRequest request = new CreateConfigurationRequest();
        request.setAppName(existing.getAppName());
        request.setEnv(existing.getEnv());
        request.setData(newData);

        // 3. Create new version (this saves to DB, invalidates Redis, etc.)
        Configuration saved = create(request);

        // 4. Publish Kafka event for update
        eventProducer.publish(
                new ConfigurationChangeEvent(
                        "CONFIG_UPDATED",
                        saved.getAppName(),
                        saved.getEnv(),
                        saved.getVersion(),
                        Instant.now()
                )
        );

        // 5. Return the newly created version
        return saved;
    }

    // SOFT DELETE
    public void delete(UUID id) {
        // 1. Load configuration (throws 404 if not found or already deleted)
        Configuration config = getById(id);

        // 2. Soft delete (mark as deleted)
        config.setDeletedAt(Instant.now());
        configurationRepository.save(config);

        // 3. Invalidate Redis cache for this app/env
        String redisKey =
                RedisKeys.latestConfig(config.getAppName(), config.getEnv());

        redisTemplate.delete(redisKey);

        eventProducer.publish(
                new ConfigurationChangeEvent(
                        "CONFIG_DELETED",
                        config.getAppName(),
                        config.getEnv(),
                        config.getVersion(),
                        Instant.now()
                )
        );
    }
}
