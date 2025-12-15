package com.emilyordanov.configmgmt.mapper;

import com.emilyordanov.configmgmt.dto.ConfigurationResponse;
import com.emilyordanov.configmgmt.entity.Configuration;

public class ConfigurationMapper {
    public static ConfigurationResponse toResponse(Configuration entity) {
        ConfigurationResponse dto = new ConfigurationResponse();
        dto.setId(entity.getId());
        dto.setAppName(entity.getAppName());
        dto.setEnv(entity.getEnv());
        dto.setVersion(entity.getVersion());
        dto.setData(entity.getData());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
