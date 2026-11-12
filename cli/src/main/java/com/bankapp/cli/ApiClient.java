package com.bankapp.cli;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Thin wrapper around the bank server's REST API. */
public class ApiClient {

    /** An error returned by the server (or a failure to reach it, status 0). */
    public static class ApiException extends Exception {
        private final int status;

        public ApiException(int status, String message) {
            super(message);
            this.status = status;
        }

        public int status() { return status; }
    }

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper mapper = new ObjectMapper()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    private final String baseUrl;
    private String token;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public void setToken(String token) { this.token = token; }

    public boolean hasToken() { return token != null; }

    public ObjectMapper mapper() { return mapper; }

    public JsonNode get(String path) throws ApiException {
        return send(request(path).GET());
    }

    public JsonNode post(String path, Object body) throws ApiException {
        try {
            String json = mapper.writeValueAsString(body);
            return send(request(path)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)));
        } catch (IOException e) {
            throw new ApiException(0, "Could not build request: " + e.getMessage());
        }
    }

    private HttpRequest.Builder request(String path) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json");
        if (token != null) {
            b.header("Authorization", "Bearer " + token);
        }
        return b;
    }

    private JsonNode send(HttpRequest.Builder builder) throws ApiException {
        HttpResponse<String> response;
        try {
            response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(0, "Request interrupted");
        } catch (IOException e) {
            throw new ApiException(0, "Cannot reach the bank server at " + baseUrl
                    + ". Is it running? (" + e.getMessage() + ")");
        }

        String body = response.body();
        int status = response.statusCode();
        JsonNode json = null;
        if (body != null && !body.isBlank()) {
            try {
                json = mapper.readTree(body);
            } catch (IOException ignored) {
                // non-JSON body; handled below
            }
        }
        if (status >= 200 && status < 300) {
            return json == null ? mapper.createObjectNode() : json;
        }
        String message = (json != null && json.hasNonNull("message"))
                ? json.get("message").asText()
                : (status == 401 ? "Not authorized" : "Request failed (HTTP " + status + ")");
        throw new ApiException(status, message);
    }
}
