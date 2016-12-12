package io.specto.hoverfly.junit.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.specto.hoverfly.junit.core.model.Simulation;
import org.junit.After;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static io.specto.hoverfly.junit.core.HoverflyMode.SIMULATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class HoverflyTest {

    private static final int EXPECTED_PROXY_PORT = 8890;
    private Hoverfly hoverfly;
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldStartHoverflyOnConfiguredPort() throws Exception {
        hoverfly = new Hoverfly(configs().proxyPort(EXPECTED_PROXY_PORT), SIMULATE);
        hoverfly.start();
        assertThat(System.getProperty("http.proxyPort")).isEqualTo(String.valueOf(EXPECTED_PROXY_PORT));
        assertThat(hoverfly.getProxyPort()).isEqualTo(EXPECTED_PROXY_PORT);
    }

    @Test
    public void shouldDeleteTempFilesWhenStoppingHoverfly() throws Exception {
        startDefaultHoverfly();
        hoverfly.stop();
        final Field binaryPath = ReflectionUtils.findField(Hoverfly.class, "binaryPath", Path.class);
        binaryPath.setAccessible(true);
        assertThat(Files.exists((Path) binaryPath.get(hoverfly))).isFalse();
    }

    @Test
    public void shouldImportSimulationObject() throws Exception {
        startDefaultHoverfly();
        // When
        URL resource = Resources.getResource("test-service.json");
        Simulation importedSimulation = mapper.readValue(resource, Simulation.class);
        hoverfly.importSimulation(importedSimulation);

        // Then
        Simulation exportedSimulation = hoverfly.getSimulation();
        assertThat(exportedSimulation).isEqualTo(importedSimulation);
    }

    @Test
    public void shouldImportFileURI() throws Exception {
        startDefaultHoverfly();
        // When
        URL resource = Resources.getResource("test-service.json");
        Simulation importedSimulation = mapper.readValue(resource, Simulation.class);
        hoverfly.importSimulation(resource.toURI());

        // Then
        Simulation exportedSimulation = hoverfly.getSimulation();
        assertThat(exportedSimulation).isEqualTo(importedSimulation);
    }

    @Test
    public void shouldThrowExceptionWhenSubmitSimulationFailed() throws Exception {

        startDefaultHoverfly();
        // When
        Throwable throwable = catchThrowable(() -> hoverfly.importSimulation(Resources.getResource("test-service-v1.json").toURI()));

        // Then
        assertThat(throwable)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to submit simulation data");
    }

    @After
    public void tearDown() throws Exception {
        if (hoverfly != null) {
            hoverfly.stop();
        }
    }

    private void startDefaultHoverfly() throws IOException, URISyntaxException {
        hoverfly = new Hoverfly(configs(), SIMULATE);
        hoverfly.start();
    }
}
