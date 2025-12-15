package com.emilyordanov.configsdk.client;

import com.emilyordanov.configsdk.dto.ConfigurationDto;
import com.emilyordanov.configsdk.properties.ConfigClientProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ConfigClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ConfigClientProperties properties;

    public ConfigClient(ConfigClientProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMillis()))
                .build();
    }

    public ConfigurationDto getLatest(String appName, String env) {

        try {
            URI uri = URI.create(
                    properties.getBaseUrl()
                            + "/api/configurations/latest"
                            + "?appName=" + appName
                            + "&env=" + env
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Failed to fetch configuration. Status: " + response.statusCode());
            }

            return objectMapper.readValue(
                    response.body(),
                    ConfigurationDto.class
            );

        } catch (Exception e) {
            throw new RuntimeException("Error calling Config Management API", e);
        }
    }
}
