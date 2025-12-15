package com.emilyordanov.configsdk.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigurationDto {
    private String appName;
    private String env;
    private Integer version;
    private JsonNode data;

    public ConfigurationDto() {}

    public String getAppName() {
        return appName;
    }

    public String getEnv() {
        return env;
    }

    public Integer getVersion() {
        return version;
    }

    public JsonNode getData() {
        return data;
    }
}
