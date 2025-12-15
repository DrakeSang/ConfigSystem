package com.emilyordanov.configmgmt.controller.api;

import com.emilyordanov.configmgmt.dto.ConfigurationResponse;
import com.emilyordanov.configmgmt.dto.CreateConfigurationRequest;
import com.emilyordanov.configmgmt.mapper.ConfigurationMapper;
import com.emilyordanov.configmgmt.service.ConfigurationService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/configurations", produces = MediaType.APPLICATION_JSON_VALUE)
public class ConfigurationController {
    private final ConfigurationService service;

    public ConfigurationController(ConfigurationService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ConfigurationResponse create(
            @Valid @RequestBody CreateConfigurationRequest request) {
        return ConfigurationMapper.toResponse(service.create(request));
    }

    @GetMapping("/latest")
    public ConfigurationResponse getLatest(
            @RequestParam String appName,
            @RequestParam String env) {
        return ConfigurationMapper.toResponse(
                service.getLatest(appName, env));
    }

    @GetMapping("/{id}")
    public ConfigurationResponse getById(@PathVariable UUID id) {
        return ConfigurationMapper.toResponse(service.getById(id));
    }

    @GetMapping
    public List<ConfigurationResponse> list(
            @RequestParam String appName,
            @RequestParam String env) {
        return service.list(appName, env)
                .stream()
                .map(ConfigurationMapper::toResponse)
                .toList();
    }

    @PutMapping("/{id}")
    public ConfigurationResponse update(
            @PathVariable UUID id,
            @RequestBody JsonNode data) {
        return ConfigurationMapper.toResponse(service.update(id, data));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
