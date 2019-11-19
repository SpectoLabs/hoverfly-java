package io.specto.hoverfly.ruletest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.specto.hoverfly.junit.core.model.RequestResponsePair;
import io.specto.hoverfly.junit.core.model.Simulation;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import io.specto.hoverfly.webserver.CaptureModeTestWebServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static io.specto.hoverfly.junit.core.HoverflyConfig.localConfigs;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class IncrementalCaptureTest {


    private static final Path SIMULATION_FILE = Paths.get("src/test/resources/hoverfly/incremental-capture.json");
    private static final String SIMULATION_FILE_NAME = "incremental-capture.json";

    private static URI webServerBaseUrl;

    @BeforeClass
    public static void beforeAll() throws Exception {

        Files.deleteIfExists(SIMULATION_FILE);
        webServerBaseUrl = CaptureModeTestWebServer.run();
    }

    @Rule
    public HoverflyRule hoverflyRule = HoverflyRule.inCaptureMode(SIMULATION_FILE_NAME,
            localConfigs()
                    .captureAllHeaders()
                    .proxyLocalHost()
                    .enableIncrementalCapture());


    private RestTemplate restTemplate = new RestTemplate();


    @Test
    public void shouldRecordFirstRequest() {
        // When
        restTemplate.getForObject(webServerBaseUrl, String.class);
    }

    @Test
    public void shouldRecordSecondRequest() {
        // When
        restTemplate.getForObject(webServerBaseUrl + "/other", String.class);
    }


    // We have to assert after the rule has executed because that's when the classpath is written to the filesystem
    @AfterClass
    public static void afterAll() throws IOException {

        // Verify captured data is expected
        final String actualSimulation = new String(Files.readAllBytes(SIMULATION_FILE), UTF_8);

        // Verify headers are captured
        ObjectMapper objectMapper = new ObjectMapper();
        Simulation simulation = objectMapper.readValue(actualSimulation, Simulation.class);
        Set<RequestResponsePair> pairs = simulation.getHoverflyData().getPairs();
        assertThat(pairs).hasSize(2);
        assertThat(pairs.iterator().next().getRequest().getHeaders()).isNotEmpty();

        CaptureModeTestWebServer.terminate();
    }

}
