package com.emilyordanov.configsdk.properties;

public class ConfigClientProperties {
    private String baseUrl;
    private int connectTimeoutMillis = 2000;
    private int readTimeoutMillis = 2000;

    public ConfigClientProperties(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }
}
