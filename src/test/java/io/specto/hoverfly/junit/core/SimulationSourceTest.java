package io.specto.hoverfly.junit.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.specto.hoverfly.junit.core.model.GlobalActions;
import io.specto.hoverfly.junit.core.model.HoverflyData;
import io.specto.hoverfly.junit.core.model.HoverflyMetaData;
import io.specto.hoverfly.junit.core.model.RequestResponsePair;
import io.specto.hoverfly.junit.core.model.Simulation;
import io.specto.hoverfly.webserver.ImportTestWebServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Set;

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
    public static void setUp() {
        url = ImportTestWebServer.run();
    }

    @AfterClass
    public static void tearDown() {
        ImportTestWebServer.terminate();
    }

    private static Simulation getSimulation() {
        try {
            return new ObjectMapper().readValue(Resources.getResource("test-service.json"), Simulation.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldCreateSimulationFromClasspath() {

        // When
        Simulation actual = SimulationSource.classpath("test-service.json").getSimulation();

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(EXPECTED);
    }

    @Test
    public void shouldCreateSimulationFromClasspathRelativeToHoverfly() {
        // When
        Simulation actual = SimulationSource.defaultPath("test-service-below-hoverfly-dir.json").getSimulation();

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(EXPECTED);
    }


    @Test
    public void shouldCreateSimulationFromUrl() {

        // When
        Simulation actual = SimulationSource.url(url).getSimulation();

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(EXPECTED);
    }

    @Test
    public void shouldCreateSimulationFromUrlString() {

        // When
        Simulation actual = SimulationSource.url(url.toString()).getSimulation();

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(EXPECTED);
    }

    @Test
    public void shouldCreateSimulationFromDsl() {

        // When
        SimulationSource simulationSource = SimulationSource.dsl(
                service("www.test-service.com").get("/foo").willReturn(success()));

        Simulation actual = simulationSource.getSimulation();

        // Then
        Set<RequestResponsePair> pairs = actual.getHoverflyData().getPairs();
        assertThat(pairs).hasSize(1);
        RequestResponsePair pair = pairs.iterator().next();
        assertThat(pair.getRequest().getDestination().getExactMatch()).isEqualTo("www.test-service.com");
        assertThat(pair.getRequest().getPath().getExactMatch()).isEqualTo("/foo");
        assertThat(pair.getResponse().getStatus()).isEqualTo(200);

    }

    @Test
    public void shouldCreateSimulationFromFile() throws Exception {

        // When
        Simulation actual = SimulationSource.file(Paths.get(Resources.getResource("test-service.json").toURI())).getSimulation();

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(EXPECTED);
    }

    @Test
    public void shouldCreateSimulationFromSimulationObject() {

        // Given
        Simulation expected = new Simulation(new HoverflyData(emptySet(), new GlobalActions(emptyList())), new HoverflyMetaData());

        // When
        Simulation actual = SimulationSource.simulation(expected).getSimulation();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldCreateEmptySimulation() {
        assertThat(SimulationSource.empty().getSimulation());
    }

    @Test
    public void shouldThrowExceptionWhenSimulationDataFormatIsNotRecognized() {

        // When
        Throwable throwable = catchThrowable(() -> SimulationSource.classpath("simulations/legacy-simulation.json").getSimulation());

        // Then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot load classpath resource: 'simulations/legacy-simulation.json'");

    }

    @Test
    public void shouldThrowExceptionWhenUrlStringIsInvalid() {

        // When
        Throwable throwable = catchThrowable(() -> SimulationSource.url("http://foo.com").getSimulation());

        // Then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot read simulation");
    }

    @Test
    public void shouldThrowExceptionWhenUrlIsInvalid() {

        // When
        Throwable throwable = catchThrowable(() -> SimulationSource.url(new URL("http://foo.com")).getSimulation());

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