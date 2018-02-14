package io.specto.hoverfly.junit.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.specto.hoverfly.junit.api.model.ModeArguments;
import io.specto.hoverfly.junit.api.view.HoverflyInfoView;
import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit.core.config.HoverflyConfiguration;
import io.specto.hoverfly.junit.core.model.FieldMatcher;
import io.specto.hoverfly.junit.core.model.Journal;
import io.specto.hoverfly.junit.core.model.Request;
import io.specto.hoverfly.junit.core.model.Simulation;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.web.client.RestTemplate;

import static io.specto.hoverfly.junit.core.HoverflyMode.CAPTURE;
import static io.specto.hoverfly.junit.core.HoverflyMode.SIMULATE;
import static org.assertj.core.api.Assertions.assertThat;

public class OkHttpHoverflyClientTest {


    private Hoverfly hoverfly;
    private OkHttpHoverflyClient client;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        startDefaultHoverfly();
        HoverflyConfiguration hoverflyConfig = hoverfly.getHoverflyConfig();
        client = new OkHttpHoverflyClient(hoverflyConfig.getScheme(), hoverflyConfig.getHost(), hoverflyConfig.getAdminPort(), null);
    }

    @Test
    public void shouldBeAbleToHealthcheck() throws Exception {
        assertThat(client.getHealth()).isTrue();
    }

    @Test
    public void shouldBeAbleToGetConfigInfo() throws Exception {
        HoverflyInfoView configInfo = client.getConfigInfo();

        assertThat(configInfo.getMode()).isEqualTo(SIMULATE.getValue());
        assertThat(configInfo.getDestination()).isEqualTo(".");
        assertThat(configInfo.getUpstreamProxy()).isEmpty();
    }

    @Test
    public void shouldBeAbleToSetDestination() throws Exception {
        client.setDestination("www.test.com");

        assertThat(hoverfly.getHoverflyInfo().getDestination()).isEqualTo("www.test.com");
    }


    @Test
    public void shouldBeAbleToSetMode() throws Exception {
        client.setMode(CAPTURE);

        assertThat(hoverfly.getMode()).isEqualTo(CAPTURE);
    }


    @Test
    public void shouldBeAbleToSetCaptureModeWithArguments() throws Exception {
        client.setMode(CAPTURE, new ModeArguments(Lists.newArrayList("Content-Type", "Authorization")));

        List<String> headersWhitelist = hoverfly.getHoverflyInfo().getModeArguments().getHeadersWhitelist();
        assertThat(headersWhitelist).hasSize(2);
        assertThat(headersWhitelist).containsOnly("Content-Type", "Authorization");
    }

    @Test
    public void shouldBeAbleToSetV1Simulation() throws Exception {
        URL resource = Resources.getResource("simulations/v1-simulation.json");
        Simulation simulation = objectMapper.readValue(resource, Simulation.class);
        client.setSimulation(simulation);

        Simulation exportedSimulation = hoverfly.getSimulation();
        assertThat(exportedSimulation.getHoverflyData()).isEqualTo(simulation.getHoverflyData());
    }

    @Test
    public void shouldBeAbleToSetV2Simulation() throws Exception {
        URL resource = Resources.getResource("simulations/v2-simulation.json");
        Simulation simulation = objectMapper.readValue(resource, Simulation.class);
        client.setSimulation(simulation);

        Simulation exportedSimulation = hoverfly.getSimulation();
        assertThat(exportedSimulation.getHoverflyData()).isEqualTo(simulation.getHoverflyData());
    }

    @Test
    public void shouldBeAbleToGetSimulation() throws Exception {
        Simulation simulation = hoverfly.getSimulation();

        assertThat(simulation).isEqualTo(SimulationSource.empty().getSimulation());
    }

    @Test
    public void shouldBeAbleToDeleteAllSimulation() throws Exception {
        URL resource = Resources.getResource("simulations/v2-simulation.json");
        Simulation simulation = objectMapper.readValue(resource, Simulation.class);
        client.setSimulation(simulation);

        client.deleteSimulation();

        Simulation result = client.getSimulation();
        assertThat(result.getHoverflyData().getPairs()).isEmpty();
    }

    @Test
    public void shouldBeAbleToDeleteJournal() throws Exception {

        try {

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForEntity("http://hoverfly.io", String.class);
        } catch (Exception ignored) {
            // Do nothing just to populate journal
        }

        assertThat(client.getJournal().getEntries()).isNotEmpty();

        client.deleteJournal();

        assertThat(client.getJournal().getEntries()).isEmpty();
    }

    @Test
    public void shouldBeAbleToGetJournal() throws Exception {

        String expected = Resources.toString(Resources.getResource("expected-journal.json"), Charset.defaultCharset());

        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForEntity("http://hoverfly.io", String.class);
        } catch (Exception ignored) {
            // Do nothing just to populate journal
        }


        Journal journal = client.getJournal();
        String actual = objectMapper.writeValueAsString(journal);

        JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
    }

    @Test
    public void shouldBeAbleToSearchJournal() throws Exception {
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
                .destination(FieldMatcher.wildCardMatches("hoverfly.*"))
                .build());

        assertThat(journal.getEntries()).hasSize(1);

        assertThat(journal.getEntries().iterator().next().getRequest().getDestination()).isEqualTo("hoverfly.io");
    }

    @After
    public void tearDown() throws Exception {
        if (hoverfly != null) {
            hoverfly.close();
        }
    }

    private void startDefaultHoverfly() {
        hoverfly = new Hoverfly(SIMULATE);
        hoverfly.start();
    }
}