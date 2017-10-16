package io.specto.hoverfly.junit5;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.model.Simulation;
import io.specto.hoverfly.junit5.api.HoverflyCapture;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
    @HoverflyCapture(path = "build/resources/test/hoverfly",
            filename = "captured-simulation.json",
            config = @HoverflyConfig(captureAllHeaders = true, proxyLocalHost = true))
    @ExtendWith(HoverflyExtension.class)
    class NestedTest {

        private OkHttpClient client = new OkHttpClient();
        @Test
        void shouldInjectCustomInstanceAsParameter(Hoverfly hoverfly) {
            hoverflyInstance = hoverfly;
            assertThat(hoverfly.getMode()).isEqualTo(HoverflyMode.CAPTURE);
            assertThat(hoverfly.getHoverflyInfo().getModeArguments().getHeadersWhitelist()).containsExactly("*");     // Capture all headers
        }

        @Test
        void shouldCaptureRequest1(Hoverfly hoverfly) throws IOException {

            final Request request = new Request.Builder()
                    .url("http://localhost:" + hoverfly.getHoverflyConfig().getAdminPort() + "/api/v2/hoverfly/mode")
                    .build();

            final Response response = client.newCall(request).execute();

            assertThat(response.code()).isEqualTo(200);
        }

        @Test
        void shouldCaptureRequest2(Hoverfly hoverfly) throws IOException {
            final Request request = new Request.Builder()
                    .url("http://localhost:" + hoverfly.getHoverflyConfig().getAdminPort() + "/api/health")
                    .build();

            final Response response = client.newCall(request).execute();

            assertThat(response.code()).isEqualTo(200);
        }
    }


    @AfterAll
    static void shouldExportCapturedSimulation() throws IOException {
        assertThat(hoverflyInstance.isHealthy()).isFalse();
        assertThat(CAPTURED_SIMULATION_FILE).exists();
        // should capture requests from both tests
        ObjectMapper objectMapper = new ObjectMapper();
        Simulation simulation = objectMapper.readValue(CAPTURED_SIMULATION_FILE.toFile(), Simulation.class);
        assertThat(simulation.getHoverflyData().getPairs()).hasSize(2);
    }
}
