package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit5.api.HoverflyCapture;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;


class HoverflyCustomCaptureTest {

    private static Hoverfly hoverflyInstance;
    private static final Path CAPTURED_SIMULATION_FILE = Paths.get("build/resources/test/hoverfly/captured-simulation.json");

    @BeforeAll
    static void cleanUpPreviousCapturedFile() throws IOException {
        Files.deleteIfExists(CAPTURED_SIMULATION_FILE);
    }

    @Nested
//    @HoverflySimulate
    @HoverflyCapture(path = "build/resources/test/hoverfly",
            filename = "captured-simulation.json",
            config = @HoverflyConfig(destination = "hoverfly.io", captureAllHeaders = true))
    @ExtendWith(HoverflyExtension.class)
    class NestedTest {

        @Test
        void shouldInjectCustomInstanceAsParameter(Hoverfly hoverfly) {
            hoverflyInstance = hoverfly;
            assertThat(hoverfly.getMode()).isEqualTo(HoverflyMode.CAPTURE);
            assertThat(hoverfly.getHoverflyInfo().getDestination()).isEqualTo("hoverfly.io");     // Capture all destinations
            assertThat(hoverfly.getHoverflyInfo().getModeArguments().getHeadersWhitelist()).containsExactly("*");     // Capture all headers
        }
    }


    @AfterAll
    static void shouldExportCapturedSimulation() {
        assertThat(hoverflyInstance.isHealthy()).isFalse();
        assertThat(CAPTURED_SIMULATION_FILE).exists();
    }
}
