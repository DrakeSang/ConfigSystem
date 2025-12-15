package com.emilyordanov.configmgmt.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateAndFetchLatestConfiguration() throws Exception {

        String app = "orders-" + UUID.randomUUID();
        String env = "itest";

        JsonNode created = createConfig(
                app,
                env,
                """
                        {
                          "timeoutMs": 5000,
                          "retries": 3,
                          "featureXEnabled": true
                        }
                        """
        );

        JsonNode latest = getLatest(app, env);

        assertEquals(created.get("version").asInt(), latest.get("version").asInt());
        assertEquals(5000, latest.get("data").get("timeoutMs").asInt());
        assertEquals(3, latest.get("data").get("retries").asInt());
        assertTrue(latest.get("data").get("featureXEnabled").asBoolean());
    }

    @Test
    void shouldReturnSameLatestPayloadOnSecondCall() throws Exception {

        String app = "billing-" + UUID.randomUUID();
        String env = "itest";

        createConfig(
                app,
                env,
                """
                        {
                          "timeoutMs": 7000,
                          "retries": 2
                        }
                        """
        );

        JsonNode first = getLatest(app, env);
        JsonNode second = getLatest(app, env);

        assertEquals(first.get("id").asText(), second.get("id").asText());
        assertEquals(first.get("version").asInt(), second.get("version").asInt());
        assertEquals(
                first.get("data").get("timeoutMs").asInt(),
                second.get("data").get("timeoutMs").asInt()
        );
    }

    @Test
    void updateShouldCreateNewVersionAndLatestShouldReflectIt() throws Exception {

        String app = "updates-" + UUID.randomUUID();
        String env = "itest";

        JsonNode created = createConfig(
                app,
                env,
                """
                        {
                          "timeoutMs": 3000,
                          "retries": 1
                        }
                        """
        );

        UUID id = UUID.fromString(created.get("id").asText());
        int oldVersion = created.get("version").asInt();

        JsonNode updated = updateConfig(
                id,
                """
                        {
                          "timeoutMs": 8000,
                          "retries": 7,
                          "featureXEnabled": true
                        }
                        """
        );

        int newVersion = updated.get("version").asInt();
        assertTrue(newVersion > oldVersion);

        JsonNode latest = getLatest(app, env);

        assertEquals(newVersion, latest.get("version").asInt());
        assertEquals(8000, latest.get("data").get("timeoutMs").asInt());
        assertEquals(7, latest.get("data").get("retries").asInt());
        assertTrue(latest.get("data").get("featureXEnabled").asBoolean());
    }

    @Test
    void deleteShouldSoftDeleteAndLatestShouldReturn404IfNoOtherVersions() throws Exception {

        String app = "delete-" + UUID.randomUUID();
        String env = "itest";

        JsonNode created = createConfig(
                app,
                env,
                """
                        {
                          "timeoutMs": 1111,
                          "retries": 9
                        }
                        """
        );

        UUID id = UUID.fromString(created.get("id").asText());

        ResponseEntity<Void> deleteResponse =
                restTemplate.exchange(
                        "/api/configurations/" + id,
                        HttpMethod.DELETE,
                        null,
                        Void.class
                );

        assertTrue(
                deleteResponse.getStatusCode() == HttpStatus.OK
                        || deleteResponse.getStatusCode() == HttpStatus.NO_CONTENT
        );

        ResponseEntity<String> latestResponse =
                restTemplate.getForEntity(
                        "/api/configurations/latest?appName=" + app + "&env=" + env,
                        String.class
                );

        assertEquals(HttpStatus.NOT_FOUND, latestResponse.getStatusCode());
    }

    // ----------------------------------------------------------------
    // Helper methods
    // ----------------------------------------------------------------

    private JsonNode createConfig(String appName, String env, String dataJson) throws Exception {

        String payload = """
                {
                  "appName": "%s",
                  "env": "%s",
                  "data": %s
                }
                """.formatted(appName, env, dataJson);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/configurations",
                        new HttpEntity<>(payload, headers),
                        String.class
                );

        assertTrue(
                response.getStatusCode() == HttpStatus.CREATED
                        || response.getStatusCode() == HttpStatus.OK,
                "Create should return 200 or 201"
        );

        assertNotNull(response.getBody());
        return objectMapper.readTree(response.getBody());
    }

    private JsonNode updateConfig(UUID id, String newDataJson) throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/api/configurations/" + id,
                        HttpMethod.PUT,
                        new HttpEntity<>(newDataJson, headers),
                        String.class
                );

        assertTrue(
                response.getStatusCode() == HttpStatus.OK
                        || response.getStatusCode() == HttpStatus.CREATED,
                "Update should return 200 or 201"
        );

        assertNotNull(response.getBody());
        return objectMapper.readTree(response.getBody());
    }

    private JsonNode getLatest(String appName, String env) throws Exception {

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/api/configurations/latest?appName=" + appName + "&env=" + env,
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        return objectMapper.readTree(response.getBody());
    }
}
