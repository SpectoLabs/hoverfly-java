package io.specto.hoverfly.testng;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.specto.hoverfly.junit.core.ObjectMapperFactory;
import io.specto.hoverfly.junit.core.model.RequestResponsePair;
import io.specto.hoverfly.junit.core.model.Simulation;
import io.specto.hoverfly.testng.api.TestNGClassRule;
import io.specto.hoverfly.webserver.CaptureModeTestWebServer;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static io.specto.hoverfly.junit.core.HoverflyConfig.localConfigs;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@Listeners(HoverflyExecutor.class)
public class CaptureModeTest {


    private static final Path RECORDED_SIMULATION_FILE = Paths.get("src/test/resources/hoverfly/recorded-simulation.json");
    private static final String RECORDED_SIMULATION_JSON = "recorded-simulation.json";
    private static final String EXPECTED_SIMULATION_JSON = "expected-simulation.json";

    @TestNGClassRule
    private static HoverflyTestNG hoverflyTestNG = HoverflyTestNG.inCaptureMode(RECORDED_SIMULATION_JSON,
            localConfigs().captureAllHeaders().proxyLocalHost());

    private URI webServerBaseUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    // We have to assert after the rule has executed because that's when the classpath is written to the filesystem
    @AfterTest
    public static void after() throws IOException, JSONException {
        // Verify captured data is expected
        final String expectedSimulation = Resources.toString(Resources.getResource(EXPECTED_SIMULATION_JSON), UTF_8);
        final String actualSimulation = new String(Files.readAllBytes(RECORDED_SIMULATION_FILE), UTF_8);
        JSONAssert.assertEquals(expectedSimulation, actualSimulation, JSONCompareMode.LENIENT);

        // Verify headers are captured
        Simulation simulation = ObjectMapperFactory.getDefaultObjectMapper().readValue(actualSimulation, Simulation.class);
        Set<RequestResponsePair> pairs = simulation.getHoverflyData().getPairs();
        assertThat(pairs.iterator().next().getRequest().getHeaders()).isNotEmpty();

        CaptureModeTestWebServer.terminate();
    }

    @BeforeTest
    public void setUp() throws Exception {
        Files.deleteIfExists(RECORDED_SIMULATION_FILE);
        webServerBaseUrl = CaptureModeTestWebServer.run();
    }

    @Test
    public void shouldRecordInteractions() {
        // When
        restTemplate.getForObject(webServerBaseUrl, String.class);
    }

}
