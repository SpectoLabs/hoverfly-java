package io.specto.hoverfly.junit.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.specto.hoverfly.junit.core.model.HoverflyInfo;
import io.specto.hoverfly.junit.core.model.Simulation;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class OkHttpHoverflyClient implements HoverflyClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OkHttpHoverflyClient.class);

    private static final String HEALTH_CHECK_PATH = "api/health";
    private static final String SIMULATION_PATH = "api/v2/simulation";
    private static final String INFO_PATH = "api/v2/hoverfly";
    private static final String DESTINATION_PATH = "api/v2/hoverfly/destination";
    private static final String MODE_PATH = "api/v2/hoverfly/mode";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client;

    private HttpUrl baseUrl;

    public OkHttpHoverflyClient(HoverflyConfig hoverflyConfig) {

        this.client = new OkHttpClient();

        this.baseUrl = new HttpUrl.Builder()
                .scheme("http")
                .host(hoverflyConfig.getHost())
                .port(hoverflyConfig.getAdminPort()).build();
    }

    @Override
    public void setSimulation(Simulation simulation) {
        try {

            final byte[] jsonContent = OBJECT_MAPPER.writeValueAsBytes(simulation);
            RequestBody body = RequestBody.create(JSON, jsonContent);

            final Request.Builder builder = createRequestBuilderWithUrl(SIMULATION_PATH);
            final Request request = builder.put(body).build();

            final Call call = client.newCall(request);
            try (final Response response = call.execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to import simulation data", e);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Simulation getSimulation() {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(SIMULATION_PATH);
            final Request request = builder.get().build();

            final Call call = client.newCall(request);
            return readSimulation(call);
        } catch (Exception e) {
            LOGGER.error("Failed to export simulation data", e);
            throw new HoverflyClientException();
        }
    }

    @Override
    public HoverflyInfo getConfigInfo() {
        final Request.Builder builder = createRequestBuilderWithUrl(INFO_PATH);
        final Request request = builder.get().build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            return OBJECT_MAPPER.readValue(response.body().charStream(), HoverflyInfo.class);
        } catch (IOException e) {
            LOGGER.error("Failed to get Hoverfly info", e);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void setDestination(String destination) {
        try {
            HoverflyInfo hoverflyInfo = new HoverflyInfo(destination, null, null, null);
            final byte[] jsonContent = OBJECT_MAPPER.writeValueAsBytes(hoverflyInfo);
            RequestBody body = RequestBody.create(JSON, jsonContent);
            final Request.Builder builder = createRequestBuilderWithUrl(DESTINATION_PATH);
            final Request request = builder.put(body).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to get Hoverfly info", e);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void setMode(HoverflyMode mode) {
        try {
            HoverflyInfo hoverflyInfo = new HoverflyInfo(null, mode.name().toLowerCase(), null, null);
            final byte[] jsonContent = OBJECT_MAPPER.writeValueAsBytes(hoverflyInfo);
            RequestBody body = RequestBody.create(JSON, jsonContent);
            final Request.Builder builder = createRequestBuilderWithUrl(MODE_PATH);
            final Request request = builder.put(body).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to get Hoverfly info", e);
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns whether the running Hoverfly is healthy or not
     */
    @Override
    public boolean getHealth() {
        final Request.Builder builder = createRequestBuilderWithUrl(HEALTH_CHECK_PATH);
        final Request request = builder.get().build();

        try {
            final Call call = client.newCall(request);
            try (final Response response = call.execute()) {
                LOGGER.debug("Hoverfly health check status code is: {}", response.code());
                return response.isSuccessful();
            }
        } catch (IOException e) {
            LOGGER.debug("Not yet healthy", e);
        }
        return false;
    }


    private Request.Builder createRequestBuilderWithUrl(String path) {
        return new Request.Builder()
                .url(baseUrl.newBuilder().addPathSegments(path).build());
    }


    private Simulation readSimulation(Call call) throws IOException {
        try (Response response = call.execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            return OBJECT_MAPPER.readValue(response.body().charStream(), Simulation.class);
        }
    }

    private class HoverflyClientException extends RuntimeException {
    }
}
