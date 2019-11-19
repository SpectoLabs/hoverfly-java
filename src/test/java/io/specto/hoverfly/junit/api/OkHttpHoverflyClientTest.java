package io.specto.hoverfly.junit.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import io.specto.hoverfly.junit.api.command.SortParams;
import io.specto.hoverfly.junit.api.model.ModeArguments;
import io.specto.hoverfly.junit.api.view.HoverflyInfoView;
import io.specto.hoverfly.junit.api.view.StateView;
import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.config.HoverflyConfiguration;
import io.specto.hoverfly.junit.core.model.Journal;
import io.specto.hoverfly.junit.core.model.JournalEntry;
import io.specto.hoverfly.junit.core.model.Request;
import io.specto.hoverfly.junit.core.model.Simulation;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static io.specto.hoverfly.junit.core.HoverflyMode.CAPTURE;
import static io.specto.hoverfly.junit.core.HoverflyMode.SIMULATE;
import static io.specto.hoverfly.junit.core.model.RequestFieldMatcher.newGlobMatcher;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class OkHttpHoverflyClientTest {


    private Hoverfly hoverfly;
    private OkHttpHoverflyClient client;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        startDefaultHoverfly();
        HoverflyConfiguration hoverflyConfig = hoverfly.getHoverflyConfig();
        client = new OkHttpHoverflyClient(hoverflyConfig.getScheme(), hoverflyConfig.getHost(), hoverflyConfig.getAdminPort(), null);
    }

    @Test
    public void shouldBeAbleToHealthcheck() {
        assertThat(client.getHealth()).isTrue();
    }

    @Test
    public void shouldBeAbleToGetConfigInfo() {
        HoverflyInfoView configInfo = client.getConfigInfo();

        assertThat(configInfo.getMode()).isEqualTo(SIMULATE.getValue());
        assertThat(configInfo.getDestination()).isEqualTo(".");
        assertThat(configInfo.getUpstreamProxy()).isEmpty();
    }

    @Test
    public void shouldBeAbleToSetDestination() {
        client.setDestination("www.test.com");

        assertThat(hoverfly.getHoverflyInfo().getDestination()).isEqualTo("www.test.com");
    }


    @Test
    public void shouldBeAbleToSetMode() {
        client.setMode(CAPTURE);

        assertThat(hoverfly.getMode()).isEqualTo(CAPTURE);
    }

    @Test
    public void shouldBeAbleToSetAndGetState() {
        final StateView setStateView = new StateView(ImmutableMap.of("key1", "value1", "key2", "value2"));
        client.setState(setStateView);

        StateView resultStateView = client.getState();

        assertThat(resultStateView)
                .as("get returns the state which have just been set.")
                .isEqualTo(setStateView);
    }

    @Test
    public void shouldBeAbleToSetAndUpdateAndGetState() {
        final StateView setStateView = new StateView(ImmutableMap.of("key1", "value1", "key2", "value2"));
        client.setState(setStateView);

        final StateView updateStateView = new StateView(ImmutableMap.of("key2", "value4", "key3", "value3"));
        client.updateState(updateStateView);

        StateView resultStateView = client.getState();

        assertThat(resultStateView)
                .as("get returns the state which have just been updated.")
                .isNotNull()
                .hasFieldOrPropertyWithValue("state", ImmutableMap.of("key1", "value1", "key2", "value4", "key3", "value3"));
    }

    @Test
    public void shouldBeAbleToSetAndDeleteState() {
        final StateView setStateView = new StateView(ImmutableMap.of("key1", "value1", "key2", "value2"));
        client.setState(setStateView);
        client.deleteState();

        StateView resultStateView = client.getState();

        assertThat(resultStateView.getState())
                .as("get returns an empty state after delete.")
                .isNotNull().isEmpty();
    }


    @Test
    public void shouldBeAbleToSetCaptureModeWithArguments() {
        ModeArguments modeArguments = new ModeArguments(Lists.newArrayList("Content-Type", "Authorization"), true);
        client.setMode(CAPTURE, modeArguments);

        ModeArguments actual = hoverfly.getHoverflyInfo().getModeArguments();
        List<String> headersWhitelist = actual.getHeadersWhitelist();
        assertThat(headersWhitelist).hasSize(2);
        assertThat(headersWhitelist).containsOnly("Content-Type", "Authorization");

        assertThat(actual.isStateful()).isTrue();
    }

    @Test
    public void shouldBeAbleToSetV1Simulation() throws Exception {
        assertSimulationIsSetAndUpgraded("simulations/v1-simulation.json");
    }

    @Test
    public void shouldBeAbleToSetV2Simulation() throws Exception {
        assertSimulationIsSetAndUpgraded("simulations/v2-simulation.json");
    }

    @Test
    public void shouldBeAbleToSetV3Simulation() throws Exception {
        assertSimulationIsSetAndUpgraded("simulations/v3-simulation.json");
    }

    @Test
    public void shouldBeAbleToSetV4Simulation() throws Exception {
        assertSimulationIsSetAndUpgraded("simulations/v4-simulation.json");
    }

    @Test
    public void shouldBeAbleToGetSimulation() {
        Simulation simulation = client.getSimulation();

        assertThat(simulation).isEqualTo(Simulation.newEmptyInstance());
    }

    @Test
    public void shouldBeAbleToGetSimulationAsJsonNode() throws JsonProcessingException {
        JsonNode simulation = client.getSimulationJson();

        assertThat(objectMapper.treeToValue(simulation, Simulation.class)).isEqualTo(Simulation.newEmptyInstance());
    }

    @Test
    public void shouldBeAbleToDeleteAllSimulation() throws Exception {
        URL resource = Resources.getResource("simulations/v5-simulation.json");
        Simulation simulation = objectMapper.readValue(resource, Simulation.class);
        client.setSimulation(simulation);

        client.deleteSimulation();

        Simulation result = client.getSimulation();
        assertThat(result.getHoverflyData().getPairs()).isEmpty();
    }

    @Test
    public void shouldBeAbleToDeleteJournal() {

        try {

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForEntity("http://hoverfly.io", String.class);
        } catch (Exception ignored) {
            // Do nothing just to populate journal
        }

        assertThat(client.getJournal(0, 10).getTotal()).isNotZero();

        client.deleteJournal();

        assertThat(client.getJournal(0, 10).getTotal()).isZero();
    }

    @Test
    public void shouldBeAbleToGetJournal() throws Exception {

        String expected = Resources.toString(Resources.getResource("expected-journal.json"), UTF_8);

        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForEntity("http://hoverfly.io", String.class);
        } catch (Exception ignored) {
            // Do nothing just to populate journal
        }


        Journal journal = client.getJournal(0, 10);
        String actual = objectMapper.writeValueAsString(journal);

        JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
    }

    @Test
    public void shouldBeAbleToGetJournalWithSortParams() {

        RestTemplate restTemplate = new RestTemplate();
        for (int i = 0; i < 3; i++) {
            try {
                restTemplate.getForEntity("http://hoverfly.io", String.class);
            } catch (Exception ignored) {
                // Do nothing just to populate journal
            }
        }

        Journal journal = client.getJournal(0, 10, new SortParams("timeStarted", SortParams.Direction.DESC));

        assertThat(journal.getTotal()).isEqualTo(3);
        List<JournalEntry> entries = journal.getEntries();
        assertThat(entries.get(0).getTimeStarted()).isAfterOrEqualTo(entries.get(1).getTimeStarted());
        assertThat(entries.get(1).getTimeStarted()).isAfterOrEqualTo(entries.get(2).getTimeStarted());
    }

    @Test
    public void shouldBeAbleToSearchJournal() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.getForEntity("http://hoverfly.io", String.class);
        } catch (Exception ignored) {
            // Do nothing just to populate journal
        }

        try {
            restTemplate.getForEntity("http://specto.io", String.class);

        } catch (Exception ignored) {
            // Do nothing just to populate journal
        }


        Journal journal = client.searchJournal(new Request.Builder()
                .destination(Collections.singletonList(newGlobMatcher("hoverfly.*")))
                .build());

        assertThat(journal.getEntries()).hasSize(1);

        assertThat(journal.getEntries().iterator().next().getRequest().getDestination()).isEqualTo("hoverfly.io");
    }

    @After
    public void tearDown() {
        if (hoverfly != null) {
            hoverfly.close();
        }
    }

    private void startDefaultHoverfly() {
        hoverfly = new Hoverfly(SIMULATE);
        hoverfly.start();
    }

    private void assertSimulationIsSetAndUpgraded(String resourcePath) throws IOException {
        URL resource = Resources.getResource(resourcePath);
        String simulation = Resources.toString(resource, UTF_8);
        client.setSimulation(simulation);

        Simulation exportedSimulation = hoverfly.getSimulation();
        assertThat(exportedSimulation.getHoverflyData().getPairs()).hasSize(1);
        assertThat(exportedSimulation.getHoverflyMetaData().getSchemaVersion()).isEqualTo("v5");
    }
}
