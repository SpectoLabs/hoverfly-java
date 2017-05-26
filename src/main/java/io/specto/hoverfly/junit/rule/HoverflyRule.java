/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this classpath except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * <p>
 * Copyright 2016-2016 SpectoLabs Ltd.
 */
package io.specto.hoverfly.junit.rule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.specto.hoverfly.junit.core.*;
import io.specto.hoverfly.junit.dsl.HoverflyDsl;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static io.specto.hoverfly.junit.core.HoverflyMode.CAPTURE;
import static io.specto.hoverfly.junit.core.HoverflyMode.SIMULATE;
import static io.specto.hoverfly.junit.core.SimulationSource.empty;
import static io.specto.hoverfly.junit.core.SimulationSource.file;
import static io.specto.hoverfly.junit.rule.HoverflyRuleUtils.*;


/**
 * <p>The {@link HoverflyRule} auto-spins up a {@link Hoverfly} process, and tears it down at the end of your tests.  It also configures the JVM
 * proxy to use {@link Hoverfly}, so so long as your client respects these proxy settings you shouldn't have to configure it.</p>
 * <h2>Example Usage</h2>
 * <pre>
 * public class SomeTest {
 *      {@code @ClassRule}
 *      public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(classpath("test-service.json"))
 *
 *      {@code @Test}
 *      public void test() { //All requests will be proxied through Hoverfly
 *          // Given
 *          {@code final RequestEntity<Void> bookFlightRequest = RequestEntity.delete(new URI("http://www.other-anotherService.com/api/bookings/1")).build();}
 *
 *          // When
 *          {@code final ResponseEntity<Void> bookFlightResponse = restTemplate.exchange(bookFlightRequest, Void.class);}
 *
 *          // Then
 *          assertThat(bookFlightResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
 *      }
 * }
 * </pre>
 * <p>You can provide data from a Hoverfly JSON simulation, or alternatively you can use a DSL - {@link HoverflyDsl}</p>
 * <p>It is also possible to capture data:</p>
 * <pre>
 *     &#064;ClassRule
 *     public static HoverflyRule hoverflyRule = HoverflyRule.inCaptureMode("recorded-simulation.json");
 * </pre>
 * <p>The recorded data will be saved in your src/test/resources/hoverfly directory</p>
 * <p><b>It's recommended to always use the {@link ClassRule} annotation, so you can share the same instance of Hoverfly through all your tests.</b>
 * This avoids the overhead of starting Hoverfly multiple times, and also helps ensure all your system properties are set before executing any other code.
 * If you want to change the data, you can do so in {@link Before} method by calling {@link HoverflyRule#simulate}, but this will not be thread safe.</p>
 *
 * @see SimulationSource
 * @see HoverflyDsl
 */
public class HoverflyRule extends ExternalResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(HoverflyRule.class);
    private static final ObjectWriter JSON_PRETTY_PRINTER = new ObjectMapper().writerWithDefaultPrettyPrinter();

    private final Hoverfly hoverfly;
    private final HoverflyMode hoverflyMode;
    private Path capturePath;
    private SimulationSource simulationSource;
    private boolean enableSimulationPrint;

    private HoverflyRule(final SimulationSource simulationSource, final HoverflyConfig hoverflyConfig) {
        this.hoverflyMode = SIMULATE;
        this.hoverfly = new Hoverfly(hoverflyConfig, hoverflyMode);
        this.simulationSource = simulationSource;
        this.capturePath = null;
    }

    private HoverflyRule(final Path capturePath, final HoverflyConfig hoverflyConfig) {
        this.hoverflyMode = CAPTURE;
        this.hoverfly = new Hoverfly(hoverflyConfig, hoverflyMode);
        this.simulationSource = null;
        this.capturePath = capturePath;
    }

    public HoverflyRule(final HoverflyConfig hoverflyConfig) {
        this.hoverflyMode = CAPTURE;
        this.hoverfly = new Hoverfly(hoverflyConfig, hoverflyMode);
        this.simulationSource = null;
        this.capturePath = null;
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in capture mode if
     * recorded file is not present, or in simulation mode if record file is present
     *
     * @param recordFile the path where captured or simulated traffic is taken. Relative to src/test/resources/hoverfly
     * @return the rule
     */
    public static HoverflyRule inCaptureOrSimulationMode(String recordFile) {
        return inCaptureOrSimulationMode(recordFile, configs());
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in capture mode if
     * recorded file is not present, or in simulation mode if record file is present
     *
     * @param recordFile     the path where captured or simulated traffic is taken. Relative to src/test/resources/hoverfly
     * @param hoverflyConfig the config
     * @return the rule
     */
    public static HoverflyRule inCaptureOrSimulationMode(String recordFile, HoverflyConfig hoverflyConfig) {
        final Path path = fileRelativeToTestResourcesHoverfly(recordFile);
        if (Files.exists(path) && Files.isRegularFile(path)) {
            return inSimulationMode(file(path), hoverflyConfig);
        } else {
            return inCaptureMode(recordFile, hoverflyConfig);
        }
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in capture mode
     *
     * @param outputFilename the path to the recorded name relative to src/test/resources/hoverfly
     * @return the rule
     */
    public static HoverflyRule inCaptureMode(String outputFilename) {
        return inCaptureMode(outputFilename, configs());
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in capture mode
     *
     * @param outputFilename the path to the recorded name relative to src/test/resources/hoverfly
     * @param hoverflyConfig the config
     * @return the rule
     */
    public static HoverflyRule inCaptureMode(String outputFilename, HoverflyConfig hoverflyConfig) {
        createTestResourcesHoverflyDirectoryIfNoneExisting();
        return new HoverflyRule(fileRelativeToTestResourcesHoverfly(outputFilename), hoverflyConfig);
    }

    public static HoverflyRule inCaptureMode() {
        return inCaptureMode(configs());
    }

    public static HoverflyRule inCaptureMode(HoverflyConfig hoverflyConfig) {
        return new HoverflyRule(hoverflyConfig);
    }


    /**
     * Instantiates a rule which runs {@link Hoverfly} in simulate mode
     *
     * @param simulationSource the simulation to import
     * @return the rule
     */
    public static HoverflyRule inSimulationMode(final SimulationSource simulationSource) {
        return inSimulationMode(simulationSource, configs());
    }

    public static HoverflyRule inSimulationMode(final SimulationSource simulationSource, final HoverflyConfig hoverflyConfig) {
        return new HoverflyRule(simulationSource, hoverflyConfig);
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in simulate mode with no data
     *
     * @return the rule
     */
    public static HoverflyRule inSimulationMode() {
        return inSimulationMode(configs());
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in simulate mode with no data
     *
     * @param hoverflyConfig the config
     * @return the rule
     */
    public static HoverflyRule inSimulationMode(final HoverflyConfig hoverflyConfig) {
        return inSimulationMode(empty(), hoverflyConfig);
    }

    /**
     * Log warning if {@link HoverflyRule} is annotated with {@link Rule}
     */
    @Override
    public Statement apply(Statement base, Description description) {
        if (isAnnotatedWithRule(description)) {
            LOGGER.warn("It is recommended to use HoverflyRule with @ClassRule to get better performance in your tests, and prevent known issue with Apache HttpClient. For more information, please see https://github.com/SpectoLabs/hoverfly-java.");
        }
        return super.apply(base, description);
    }

    /**
     * Starts in instance of Hoverfly
     */
    @Override
    protected void before() throws Throwable {
        hoverfly.start();

        importSimulationSource();

        if (enableSimulationPrint) {
            System.out.println("Hoverfly is started with the following simulation: \n"
                    + JSON_PRETTY_PRINTER.writeValueAsString(simulationSource.getSimulation()));
        }
    }

    /**
     * Stops the managed instance of Hoverfly
     */
    @Override
    protected void after() {
        try {
            if (hoverflyMode == CAPTURE) {
                hoverfly.exportSimulation(capturePath);
            }
        } finally {
            hoverfly.close();
        }
    }

    /**
     * Gets the proxy port this has run on, which could be useful when running {@link Hoverfly} on a random port.
     *
     * @return the proxy port
     */
    public int getProxyPort() {
        return hoverfly.getHoverflyConfig().getProxyPort();
    }

    /**
     * Gets started Hoverfly mode
     *
     * @return the mode.
     */
    public HoverflyMode getHoverflyMode() {
        return hoverflyMode;
    }


    /**
     * Changes the Simulation used by {@link Hoverfly}
     *
     * @param simulationSource the simulation
     */
    public void simulate(SimulationSource simulationSource) {
        if (simulationSource == null) {
            simulationSource = empty();
        }
        this.simulationSource = simulationSource;
        importSimulationSource();
    }

    /**
     * Stores what's currently been captured in the currently assigned file, wipes the simulation, then starts capture again
     * ready to store in the new file once complete.
     * @param recordFile the path where captured or simulated traffic is taken. Relative to src/test/resources/hoverfly
     */
    public void capture(final String recordFile) {
        if (hoverfly.getMode() == CAPTURE) {
            if (capturePath != null) {
                hoverfly.exportSimulation(capturePath);
            }
            hoverfly.importSimulation(empty());
            capturePath = fileRelativeToTestResourcesHoverfly(recordFile);
        }
    }

    /**
     * Get custom Hoverfly header name used by Http client to authenticate with secured Hoverfly proxy
     * @return the custom Hoverfly authorization header name
     */
    public String getAuthHeaderName() {
        return HoverflyConstants.X_HOVERFLY_AUTHORIZATION;
    }


    /**
     * Get Bearer token used by Http client to authenticate with secured Hoverfly proxy
     * @return a custom Hoverfly authorization header value
     */
    public String getAuthHeaderValue() {
        Optional<String> authToken = hoverfly.getHoverflyConfig().getAuthToken();
        return authToken.map(s -> "Bearer " + s).orElse(null);
    }

    /**
     * Print the simulation data to console for debugging purpose. This can be set when you are building the HoverflyRule
     * @return this HoverflyRule
     */
    public HoverflyRule printSimulationData() {
        if (hoverflyMode == SIMULATE) {
            enableSimulationPrint = true;
        }
        return this;
    }

    private void importSimulationSource() {
        if (hoverfly.getMode() == SIMULATE) {
            hoverfly.importSimulation(simulationSource);
        }
    }
}
