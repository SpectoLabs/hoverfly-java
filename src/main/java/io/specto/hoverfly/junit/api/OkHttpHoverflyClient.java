package io.specto.hoverfly.junit.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.specto.hoverfly.junit.api.command.DestinationCommand;
import io.specto.hoverfly.junit.api.command.ModeCommand;
import io.specto.hoverfly.junit.api.model.ModeArguments;
import io.specto.hoverfly.junit.api.view.HoverflyInfoView;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.model.Simulation;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
    public HoverflyInfoView getConfigInfo() {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(INFO_PATH);
            final Request request = builder.get().build();
            return exchange(request, HoverflyInfoView.class);
        } catch (Exception e) {
            LOGGER.warn("Failed to get config information: {}", e.getMessage());
            throw new HoverflyClientException("Failed to get config information: " + e.getMessage());
        }
    }

    @Override
    public void setDestination(String destination) {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(DESTINATION_PATH);
            final RequestBody body = createRequestBody(new DestinationCommand(destination));
            final Request request = builder.put(body).build();

            exchange(request);
        } catch (Exception e) {
            LOGGER.warn("Failed to set destination: {}", e.getMessage());
            throw new HoverflyClientException("Failed to set destination: " + e.getMessage());
        }
    }

    @Override
    public void setMode(HoverflyMode mode) {
        putModeRequest(new ModeCommand(mode));
    }

    @Override
    public void setMode(HoverflyMode mode, ModeArguments modeArguments) {
        putModeRequest(new ModeCommand(mode, modeArguments));
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

    private void putModeRequest(ModeCommand modeCommand) {
        try {
            final RequestBody body = createRequestBody(modeCommand);
            final Request.Builder builder = createRequestBuilderWithUrl(MODE_PATH);
            final Request request = builder.put(body).build();

            exchange(request);
        } catch (IOException e) {
            LOGGER.warn("Failed to set mode: {}", e.getMessage());
            throw new HoverflyClientException("Failed to set mode: " + e.getMessage());
        }
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
