package io.specto.hoverfly.junit.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.specto.hoverfly.junit.core.model.*;
import io.specto.hoverfly.webserver.ImportTestWebServer;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Set;

import static io.specto.hoverfly.assertions.Assertions.assertThat;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.catchThrowable;


public class SimulationSourceTest {

    private static final String EXPECTED = getSimulation();
    private static URL url;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeClass
    public static void setUp() {
        url = ImportTestWebServer.run();
    }

    @AfterClass
    public static void tearDown() {
        ImportTestWebServer.terminate();
    }

    private static String getSimulation() {
        try {
            return Resources.toString(Resources.getResource("test-service.json"), UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldCreateSimulationFromClasspath() {

        // When
        String actual = SimulationSource.classpath("test-service.json").getSimulation();

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(EXPECTED);
    }

    @Test
    public void shouldCreateSimulationFromClasspathRelativeToHoverfly() {
        // When
        String actual = SimulationSource.defaultPath("test-service-below-hoverfly-dir.json").getSimulation();

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(EXPECTED);
    }


    @Test
    public void shouldCreateSimulationFromUrl() {

        // When
        String actual = SimulationSource.url(url).getSimulation();

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(EXPECTED);
    }

    @Test
    public void shouldCreateSimulationFromUrlString() {

        // When
        String actual = SimulationSource.url(url.toString()).getSimulation();

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(EXPECTED);
    }

    @Test
    public void shouldCreateSimulationFromDsl() throws Exception {

        // When
        SimulationSource simulationSource = SimulationSource.dsl(
                service("www.test-service.com").get("/foo").willReturn(success()));

        String actual = simulationSource.getSimulation();

        // Then
        Simulation simulation = objectMapper.readValue(actual, Simulation.class);
        Set<RequestResponsePair> pairs = simulation.getHoverflyData().getPairs();
        assertThat(pairs).hasSize(1);
        RequestResponsePair pair = pairs.iterator().next();
        assertThat(pair.getRequest()).hasDestinationContainsOneExactMatcher("www.test-service.com");
        assertThat(pair.getRequest()).hasPathContainsOneExactMatcher("/foo");
        assertThat(pair.getResponse().getStatus()).isEqualTo(200);

    }

    @Test
    public void shouldCreateSimulationFromFile() throws Exception {

        // When
        String actual = SimulationSource.file(Paths.get(Resources.getResource("test-service.json").toURI())).getSimulation();

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(EXPECTED);
    }

    @Test
    public void shouldCreateSimulationFromSimulationObject() throws Exception {

        // Given
        Simulation simulation = new Simulation(new HoverflyData(emptySet(), new GlobalActions(emptyList())), new HoverflyMetaData());
        String expected = objectMapper.writeValueAsString(simulation);

        // When
        String actual = SimulationSource.simulation(simulation).getSimulation();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldCreateEmptySimulation() throws JSONException {
        JSONAssert.assertEquals(
                SimulationSource.empty().getSimulation(),
                "{\"data\":{\"pairs\":[],\"globalActions\":{\"delays\":[]}},\"meta\":{\"schemaVersion\":\"v5\"}}",
                false);
    }


    @Test
    public void shouldThrowExceptionWhenUrlStringIsInvalid() {

        // When
        Throwable throwable = catchThrowable(() -> SimulationSource.url("htttp://foo.com").getSimulation());

        // Then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot read simulation");
    }

    @Test
    public void shouldThrowExceptionWhenUrlResourceIsNotAvailable() {

        // When
        Throwable throwable = catchThrowable(() -> SimulationSource.url(new URL("http://localhost:12345/simulation")).getSimulation());

        // Then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot read simulation");
    }

    @Test
    public void shouldThrowExceptionWhenFilePathIsInvalid() {

        // When
        Throwable throwable = catchThrowable(() -> SimulationSource.file(Paths.get("foo")).getSimulation());

        // Then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot load file resource: 'foo'");
    }
}
