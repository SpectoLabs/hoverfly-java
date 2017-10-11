package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit5.api.HoverflyCapture;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class HoverflyDefaultCaptureTest {

    private static Hoverfly hoverflyInstance;
    private static final Path CAPTURED_SIMULATION_FILE = Paths.get("src/test/resources/hoverfly/io_specto_hoverfly_junit5_HoverflyDefaultCaptureTest_NestedTest.json");

    @BeforeAll
    static void cleanUpPreviousCapturedFile() throws IOException {
        Files.deleteIfExists(CAPTURED_SIMULATION_FILE);
    }

    @Nested
    @HoverflyCapture
    @ExtendWith(HoverflyCaptureResolver.class)
    class NestedTest {

        @Test
        void shouldInjectDefaultInstanceAsParameter(Hoverfly hoverfly) {
            hoverflyInstance = hoverfly;
            assertThat(hoverfly.getMode()).isEqualTo(HoverflyMode.CAPTURE);
            assertThat(hoverfly.getHoverflyInfo().getModeArguments().getHeadersWhitelist()).isNull();   // Not capturing any request headers
            assertThat(hoverfly.getHoverflyInfo().getDestination()).isEqualTo(".");     // Capture all destinations
        }
    }


    @AfterAll
    static void shouldExportCapturedSimulation() {
        assertThat(hoverflyInstance.isHealthy()).isFalse();
        assertThat(CAPTURED_SIMULATION_FILE).exists();
    }
}
