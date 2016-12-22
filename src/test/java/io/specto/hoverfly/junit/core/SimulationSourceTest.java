package io.specto.hoverfly.junit.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.specto.hoverfly.junit.core.model.*;
import io.specto.hoverfly.webserver.ImportTestWebServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Set;

import static io.specto.hoverfly.junit.core.SimulationSource.classpath;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


public class SimulationSourceTest {

    private static final Simulation EXPECTED = getSimulation();
    private static URL url;

    @BeforeClass
    public static void setUp() throws Exception {
        url = ImportTestWebServer.run();
    }

    @Test
    public void shouldCreateSimulationFromClasspath() throws Exception {

        // When
        Simulation actual = SimulationSource.classpath("test-service.json").getSimulation().get();

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(EXPECTED);
    }

    @Test
    public void shouldCreateSimulationFromUrl() throws Exception {

        // When
        Simulation actual = SimulationSource.url(url).getSimulation().get();

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(EXPECTED);
    }

    @Test
    public void shouldCreateSimulationFromUrlString() throws Exception {

        // When
        Simulation actual = SimulationSource.url(url.toString()).getSimulation().get();

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(EXPECTED);
    }

    @Test
    public void shouldCreateSimulationFromDsl() throws Exception {

        // When
        SimulationSource simulationSource = SimulationSource.dsl(
                service("www.test-service.com").get("/foo").willReturn(success()));

        Simulation actual = simulationSource.getSimulation().get();

        // Then
        Set<RequestResponsePair> pairs = actual.getHoverflyData().getPairs();
        assertThat(pairs).hasSize(1);
        RequestResponsePair pair = pairs.iterator().next();
        assertThat(pair.getRequest().getDestination()).isEqualTo("www.test-service.com");
        assertThat(pair.getRequest().getPath()).isEqualTo("/foo");
        assertThat(pair.getResponse().getStatus()).isEqualTo(200);

    }

    @Test
    public void shouldCreateSimulationFromFile() throws Exception {

        // When
        Simulation actual = SimulationSource.file(Paths.get("src/test/resources", "test-service.json")).getSimulation().get();

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(EXPECTED);
    }

    @Test
    public void shouldCreateSimulationFromSimulationObject() throws Exception {

        // Given
        Simulation expected = new Simulation(new HoverflyData(emptySet(), new GlobalActions(emptyList())), new HoverflyMetaData());

        // When
        Simulation actual = SimulationSource.simulation(expected).getSimulation().get();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldCreateEmptySimulation() throws Exception {
        assertThat(SimulationSource.empty().getSimulation()).isEmpty();
    }

    @Test
    public void shouldThrowExceptionWhenSimulationDataFormatIsNotRecognized() throws Exception {

        // When
        Throwable throwable = catchThrowable(() -> SimulationSource.classpath("test-service-v1.json").getSimulation().get());

        // Then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot load classpath resource: 'test-service-v1.json'");

    }

    @Test
    public void shouldThrowExceptionWhenUrlStringIsInvalid() throws Exception {

        // When
        Throwable throwable = catchThrowable(() -> SimulationSource.url("http://foo.com").getSimulation().get());

        // Then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot read simulation");
    }

    @Test
    public void shouldThrowExceptionWhenUrlIsInvalid() throws Exception {

        // When
        Throwable throwable = catchThrowable(() -> SimulationSource.url(new URL("http://foo.com")).getSimulation().get());

        // Then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot read simulation");
    }

    @Test
    public void shouldThrowExceptionWhenFilePathIsInvalid() throws Exception {

        // When
        Throwable throwable = catchThrowable(() -> SimulationSource.file(Paths.get("foo")).getSimulation().get());

        // Then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot load file resource: 'foo'");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        ImportTestWebServer.terminate();
    }


    private static Simulation getSimulation() {
        try {
            return new ObjectMapper().readValue(Resources.getResource("test-service.json"), Simulation.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}