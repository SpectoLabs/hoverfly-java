package io.specto.hoverfly.junit.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.specto.hoverfly.junit.core.HoverflyConfig;
import io.specto.hoverfly.junit.core.HoverflyConfiguration;
import io.specto.hoverfly.junit.core.HoverflyConstants;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.model.HoverflyInfo;
import io.specto.hoverfly.junit.core.model.Simulation;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

class OkHttpHoverflyClient implements HoverflyClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HoverflyClient.class);

    private static final String HEALTH_CHECK_PATH = "api/health";
    private static final String SIMULATION_PATH = "api/v2/simulation";
    private static final String INFO_PATH = "api/v2/hoverfly";
    private static final String DESTINATION_PATH = "api/v2/hoverfly/destination";
    private static final String MODE_PATH = "api/v2/hoverfly/mode";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final MediaType JSON = MediaType.parse("application/json");

    private OkHttpClient client;

    private HttpUrl baseUrl;

    OkHttpHoverflyClient(String scheme, String host, int port, String authToken) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (authToken != null ) {
            clientBuilder.addInterceptor(new AuthHeaderInterceptor(authToken));
        }
        this.client = clientBuilder.build();
        this.baseUrl = new HttpUrl.Builder()
                .scheme(scheme)
                .host(host)
                .port(port)
                .build();
    }

    @Override
    public void setSimulation(Simulation simulation) {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(SIMULATION_PATH);
            final RequestBody body = createRequestBody(simulation);
            final Request request = builder.put(body).build();

            exchange(request);
        } catch (Exception e) {
            LOGGER.warn("Failed to set simulation: {}", e.getMessage());
            throw new HoverflyClientException("Failed to set simulation: " + e.getMessage());
        }
    }

    @Override
    public Simulation getSimulation() {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(SIMULATION_PATH);
            final Request request = builder.get().build();

            return exchange(request, Simulation.class);
        } catch (Exception e) {
            LOGGER.warn("Failed to get simulation: {}", e.getMessage());
            throw new HoverflyClientException("Failed to get simulation: " + e.getMessage());
        }
    }

    @Override
    public HoverflyInfo getConfigInfo() {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(INFO_PATH);
            final Request request = builder.get().build();
            return exchange(request, HoverflyInfo.class);
        } catch (Exception e) {
            LOGGER.warn("Failed to get config information: {}", e.getMessage());
            throw new HoverflyClientException("Failed to get config information: " + e.getMessage());
        }
    }

    @Override
    public void setDestination(String destination) {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(DESTINATION_PATH);
            final RequestBody body = createRequestBody(new HoverflyInfo(destination, null, null, null));
            final Request request = builder.put(body).build();

            exchange(request);
        } catch (Exception e) {
            LOGGER.warn("Failed to set destination: {}", e.getMessage());
            throw new HoverflyClientException("Failed to set destination: " + e.getMessage());
        }
    }

    @Override
    public void setMode(HoverflyMode mode) {
        try {
            final RequestBody body = createRequestBody(new HoverflyInfo(null, mode.name().toLowerCase(), null, null));
            final Request.Builder builder = createRequestBuilderWithUrl(MODE_PATH);
            final Request request = builder.put(body).build();

            exchange(request);
        } catch (IOException e) {
            LOGGER.warn("Failed to set mode: {}", e.getMessage());
            throw new HoverflyClientException("Failed to set mode: " + e.getMessage());
        }
    }

    @Override
    public boolean getHealth() {
        boolean isHealthy = false;
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(HEALTH_CHECK_PATH);
            final Request request = builder.get().build();
            exchange(request);
            isHealthy = true;
        } catch (Exception e) {
            LOGGER.debug("Hoverfly healthcheck failed: " + e.getMessage());
        }
        return isHealthy;
    }

    private Request.Builder createRequestBuilderWithUrl(String path) {
        return new Request.Builder()
                .url(baseUrl.newBuilder().addPathSegments(path).build());
    }


    private RequestBody createRequestBody(Object data) throws JsonProcessingException {
        String content = OBJECT_MAPPER.writeValueAsString(data);
        return RequestBody.create(JSON, content);
    }


    // Deserialize response body on success
    private <T> T exchange(Request request, Class<T> clazz) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            onFailure(response);
            return OBJECT_MAPPER.readValue(response.body().string(), clazz);
        }

    }

    // Does nothing on success
    private void exchange(Request request) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            onFailure(response);
        }
    }

    private void onFailure(Response response) throws IOException {
        if (!response.isSuccessful()) {
            String errorResponse = String.format("Unexpected response (code=%d, message=%s)", response.code(), response.body().string());
            throw new IOException(errorResponse);
        }
    }
}
