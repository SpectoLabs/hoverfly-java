package io.specto.hoverfly.junit.api;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.specto.hoverfly.junit.api.command.DestinationCommand;
import io.specto.hoverfly.junit.api.command.JournalSearchCommand;
import io.specto.hoverfly.junit.api.command.ModeCommand;
import io.specto.hoverfly.junit.api.command.SortParams;
import io.specto.hoverfly.junit.api.model.ModeArguments;
import io.specto.hoverfly.junit.api.view.DiffView;
import io.specto.hoverfly.junit.api.view.HoverflyInfoView;
import io.specto.hoverfly.junit.api.view.StateView;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.model.Journal;
import io.specto.hoverfly.junit.core.model.Simulation;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OkHttpHoverflyClient implements HoverflyClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HoverflyClient.class);

    private static final String HEALTH_CHECK_PATH = "api/health";
    private static final String SIMULATION_PATH = "api/v2/simulation";
    private static final String INFO_PATH = "api/v2/hoverfly";
    private static final String DESTINATION_PATH = "api/v2/hoverfly/destination";
    private static final String MODE_PATH = "api/v2/hoverfly/mode";
    private static final String JOURNAL_PATH = "api/v2/journal";
    private static final String STATE_PATH = "api/v2/state";
    private static final String DIFF_PATH = "api/v2/diff";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final MediaType JSON = MediaType.parse("application/json");

    private final OkHttpClient client;

    private final HttpUrl baseUrl;

    OkHttpHoverflyClient(String scheme, String host, int port, String authToken) {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (authToken != null) {
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
    public void setSimulation(String simulation) {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(SIMULATION_PATH);
            final RequestBody body = RequestBody.create(JSON, simulation);
            final Request request = builder.put(body).build();
            exchange(request);
        } catch (Exception e) {
            LOGGER.warn("Failed to set simulation: {}", e.getMessage());
            throw new HoverflyClientException("Failed to set simulation: " + e.getMessage());
        }
    }

    @Override
    public void addSimulation(Simulation simulation) {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(SIMULATION_PATH);
            final RequestBody body = createRequestBody(simulation);
            final Request request = builder.post(body).build();
            exchange(request);
        } catch (Exception e) {
            LOGGER.warn("Failed to add simulation: {}", e.getMessage());
            throw new HoverflyClientException("Failed to add simulation: " + e.getMessage());
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
    public JsonNode getSimulationJson() {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(SIMULATION_PATH);
            final Request request = builder.get().build();
            try (Response response = client.newCall(request).execute()) {
                onFailure(response);
                return OBJECT_MAPPER.readTree(response.body().string());
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to get simulation: {}", e.getMessage());
            throw new HoverflyClientException("Failed to get simulation: " + e.getMessage());
        }
    }

    @Override
    public void deleteSimulation() {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(SIMULATION_PATH);
            final Request request = builder.delete().build();
            exchange(request);
        } catch (Exception e) {
            LOGGER.warn("Failed to delete simulation: {}", e.getMessage());
            throw new HoverflyClientException("Failed to delete simulation: " + e.getMessage());
        }
    }

    @Override
    public Journal getJournal(int offset, int limit) {
        return getJournalInternal(offset, limit, null);
    }

    @Override
    public Journal getJournal(int offset, int limit, SortParams sortParams) {
        return getJournalInternal(offset, limit, sortParams);
    }

    @Override
    public Journal searchJournal(io.specto.hoverfly.junit.core.model.Request requestMatcher) {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(JOURNAL_PATH);
            final RequestBody body = createRequestBody(new JournalSearchCommand(requestMatcher));
            final Request request = builder.post(body).build();
            return exchange(request, Journal.class);
        } catch (Exception e) {
            LOGGER.warn("Failed to search journal: {}", e.getMessage());
            throw new HoverflyClientException("Failed to search journal: " + e.getMessage());
        }
    }


    @Override
    public void deleteJournal() {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(JOURNAL_PATH);
            final Request request = builder.delete().build();
            exchange(request);
        } catch (Exception e) {
            LOGGER.warn("Failed to delete journal: {}", e.getMessage());
            throw new HoverflyClientException("Failed to delete journal: " + e.getMessage());
        }
    }

    @Override
    public void deleteState() {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(STATE_PATH);
            final Request request = builder.delete().build();
            exchange(request);
        } catch (Exception e) {
            LOGGER.warn("Failed to delete states: {}", e.getMessage());
            throw new HoverflyClientException("Failed to delete states: " + e.getMessage());
        }
    }

    @Override
    public StateView getState() {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(STATE_PATH);
            final Request request = builder.get().build();
            return exchange(request, StateView.class);
        } catch (Exception e) {
            LOGGER.warn("Failed to get state: {}", e.getMessage());
            throw new HoverflyClientException("Failed to get state: " + e.getMessage());
        }
    }

    @Override
    public DiffView getDiffs() {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(DIFF_PATH);
            final Request request = builder.get().build();
            return exchange(request, DiffView.class);
        } catch (Exception e) {
            LOGGER.warn("Failed to get diffs: {}", e.getMessage());
            throw new HoverflyClientException("Failed to get diffs: " + e.getMessage());
        }
    }

    @Override
    public void setState(final StateView stateView) {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(STATE_PATH);
            final RequestBody body = createRequestBody(stateView);
            final Request request = builder.put(body).build();
            exchange(request);
        } catch (Exception e) {
            LOGGER.warn("Failed to set states: {}", e.getMessage());
            throw new HoverflyClientException("Failed to set states: " + e.getMessage());
        }
    }

    @Override
    public void updateState(final StateView stateView) {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(STATE_PATH);
            final RequestBody body = createRequestBody(stateView);
            final Request request = builder.patch(body).build();
            exchange(request);
        } catch (Exception e) {
            LOGGER.warn("Failed to update states: {}", e.getMessage());
            throw new HoverflyClientException("Failed to update states: " + e.getMessage());
        }
    }

    @Override
    public void cleanDiffs() {
        try {
            final Request.Builder builder = createRequestBuilderWithUrl(DIFF_PATH);
            final Request request = builder.delete().build();
            exchange(request);
        } catch (Exception e) {
            LOGGER.warn("Failed to delete diffs: {}", e.getMessage());
            throw new HoverflyClientException("Failed to delete diffs: " + e.getMessage());
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

    private Journal getJournalInternal(int offset, int limit, SortParams sortParams) {
        try {
            HttpUrl.Builder urlBuilder = baseUrl.newBuilder()
                    .addPathSegments(JOURNAL_PATH)
                    .addQueryParameter("offset", String.valueOf(offset))
                    .addQueryParameter("limit", String.valueOf(limit));

            if (sortParams != null) {
                urlBuilder.addQueryParameter("sort", sortParams.toString());
            }
            final Request.Builder builder = new Request.Builder()
                    .url(urlBuilder.build());
            final Request request = builder.get().build();
            return exchange(request, Journal.class);
        } catch (Exception e) {
            LOGGER.warn("Failed to get journal: {}", e.getMessage());
            throw new HoverflyClientException("Failed to get journal: " + e.getMessage());
        }
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

    // Create request builder from Admin API path
    private Request.Builder createRequestBuilderWithUrl(String path) {
        return new Request.Builder()
                .url(baseUrl.newBuilder().addPathSegments(path).build());
    }


    // Convert object to JSON request body
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

    // Handle non-successful response
    private void onFailure(Response response) throws IOException {
        if (!response.isSuccessful()) {
            String errorResponse = String.format("Unexpected response (code=%d, message=%s)", response.code(), response.body().string());
            throw new IOException(errorResponse);
        }
    }
}
