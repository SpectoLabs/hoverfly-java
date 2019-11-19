package io.specto.hoverfly.junit.core;

import com.google.common.io.Resources;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import io.specto.hoverfly.webserver.CaptureModeTestWebServer;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.specto.hoverfly.junit.core.HoverflyConfig.localConfigs;
import static java.nio.charset.StandardCharsets.UTF_8;

public class MultiCaptureTest {

    private static final Path FIRST_RECORDED_SIMULATION_FILE = Paths.get("src/test/resources/hoverfly/first-multi-capture/first-multi-capture-scenario.json");
    private static final Path SECOND_RECORDED_SIMULATION_FILE = Paths.get("src/test/resources/hoverfly/second-multi-capture-scenario.json");
    private static final Path THIRD_RECORDED_SIMULATION_FILE = Paths.get("src/test/resources/hoverfly/third-multi-capture/another/third-multi-capture-scenario.json");
    private static final Path FORTH_RECORDED_SIMULATION_FILE = Paths.get("src/integration-test/resources/hoverfly/forth-multi-capture-scenario.json");
    private static final String EXPECTED_SIMULATION_JSON = "expected-simulation.json";
    private static final String OTHER_EXPECTED_SIMULATION_JSON = "expected-simulation-other.json";

    @Rule
    public HoverflyRule hoverflyRule = HoverflyRule.inCaptureMode(localConfigs().proxyLocalHost());

    private URI webServerBaseUrl;
    private RestTemplate restTemplate = new RestTemplate();

    @Before
    public void setUp() throws Exception {

        // Delete directory and contents
        final File firstSimulationDirectory = FIRST_RECORDED_SIMULATION_FILE.getParent().toFile();
        if(firstSimulationDirectory.exists()) {
            FileUtils.forceDelete(firstSimulationDirectory);
        }

        // Delete individual file
        Files.deleteIfExists(SECOND_RECORDED_SIMULATION_FILE);
        Files.deleteIfExists(FORTH_RECORDED_SIMULATION_FILE);

        // Delete directory and contents
        final File thirdSimulationDirectory = THIRD_RECORDED_SIMULATION_FILE.getParent().toFile();
        if(thirdSimulationDirectory.exists()) {
            FileUtils.forceDelete(thirdSimulationDirectory);
        }

        webServerBaseUrl = CaptureModeTestWebServer.run();
    }

    @Test
    public void shouldRecordMultipleScenariosInDifferentDirectories() {
        // Given
        hoverflyRule.capture("first-multi-capture/first-multi-capture-scenario.json");

        // When
        restTemplate.getForObject(webServerBaseUrl, String.class);

        // Given
        hoverflyRule.capture("second-multi-capture-scenario.json");

        // When
        restTemplate.getForObject(webServerBaseUrl + "/other", String.class);

        // Given
        hoverflyRule.capture("third-multi-capture/another/third-multi-capture-scenario.json");

        // When
        restTemplate.getForObject(webServerBaseUrl, String.class);

        // Given
        hoverflyRule.capture("src/integration-test/resources/hoverfly", "forth-multi-capture-scenario.json");

        // When
        restTemplate.getForObject(webServerBaseUrl, String.class);
    }

    // We have to assert after the rule has executed because that's when the classpath is written to the filesystem
    @AfterClass
    public static void after() throws IOException, JSONException {
        final String expectedSimulation = Resources.toString(Resources.getResource(EXPECTED_SIMULATION_JSON), UTF_8);
        final String otherExpectedSimulation = Resources.toString(Resources.getResource(OTHER_EXPECTED_SIMULATION_JSON), UTF_8);

        final String firstActualSimulation = new String(Files.readAllBytes(FIRST_RECORDED_SIMULATION_FILE), UTF_8);
        final String secondActualSimulation = new String(Files.readAllBytes(SECOND_RECORDED_SIMULATION_FILE), UTF_8);
        final String thirdActualSimulation = new String(Files.readAllBytes(THIRD_RECORDED_SIMULATION_FILE), UTF_8);
        final String forthActualSimulation = new String(Files.readAllBytes(FORTH_RECORDED_SIMULATION_FILE), UTF_8);

        JSONAssert.assertEquals(expectedSimulation, firstActualSimulation, JSONCompareMode.LENIENT);
        JSONAssert.assertEquals(otherExpectedSimulation, secondActualSimulation, JSONCompareMode.LENIENT);
        JSONAssert.assertEquals(expectedSimulation, thirdActualSimulation, JSONCompareMode.LENIENT);
        JSONAssert.assertEquals(expectedSimulation, forthActualSimulation, JSONCompareMode.LENIENT);

        CaptureModeTestWebServer.terminate();
    }


}
