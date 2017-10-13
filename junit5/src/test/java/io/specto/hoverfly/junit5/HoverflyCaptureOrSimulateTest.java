package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import io.specto.hoverfly.junit5.api.HoverflySimulate.Source;
import io.specto.hoverfly.junit5.api.HoverflySimulate.SourceType;
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

class HoverflyCaptureOrSimulateTest {

    private static Hoverfly hoverflyInstance;
    private static final Path CAPTURED_SIMULATION_FILE = Paths.get("build/resources/test/hoverfly/missing-simulation.json");

    @BeforeAll
    static void cleanUpPreviousCapturedFile() throws IOException {
        Files.deleteIfExists(CAPTURED_SIMULATION_FILE);
    }

    @Nested
    @HoverflySimulate(source = @Source(value = "build/resources/test/hoverfly/missing-simulation.json", type = SourceType.FILE),
        enableAutoCapture = true)
    @ExtendWith(HoverflyExtension.class)
    class CaptureIfFileNotPresent {

        @Test
        void shouldChangeModeToCapture(Hoverfly hoverfly) {
            hoverflyInstance = hoverfly;
            assertThat(hoverfly.getMode()).isEqualTo(HoverflyMode.CAPTURE);
        }
    }

    @Nested
    @HoverflySimulate(source = @Source(value = "src/test/resources/test-service-https.json", type = SourceType.FILE),
            enableAutoCapture = true)
    @ExtendWith(HoverflyExtension.class)
    class SimulateIfFilePresent {

        @Test
        void shouldBeSimulateMode(Hoverfly hoverfly) {
            assertThat(hoverfly.getMode()).isEqualTo(HoverflyMode.SIMULATE);
        }
    }


    @AfterAll
    static void shouldExportCapturedSimulation() {
        assertThat(hoverflyInstance.isHealthy()).isFalse();
        assertThat(CAPTURED_SIMULATION_FILE).exists();
    }
}
