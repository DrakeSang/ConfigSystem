package com.emilyordanov.configupdateprocessor.event;

import java.time.Instant;

public class ConfigurationChangeEvent {
    private String eventType;
    private String appName;
    private String env;
    private Integer version;
    private Instant timestamp;

    public ConfigurationChangeEvent() {
    }

    public ConfigurationChangeEvent(String eventType, String appName, String env, Integer version, Instant timestamp) {
        this.eventType = eventType;
        this.appName = appName;
        this.env = env;
        this.version = version;
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
