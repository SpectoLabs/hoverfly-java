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

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyConfig;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit.dsl.HoverflyDsl;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static io.specto.hoverfly.junit.core.HoverflyMode.CAPTURE;
import static io.specto.hoverfly.junit.core.HoverflyMode.SIMULATE;
import static io.specto.hoverfly.junit.rule.HoverflyRuleUtils.fileRelativeToTestResources;
import static io.specto.hoverfly.junit.rule.HoverflyRuleUtils.isAnnotatedWithRule;


/**
 * <p>The {@link HoverflyRule} auto-spins up a {@link Hoverfly} process, and tears it down at the end of your tests.  It also configures the JVM
 * proxy to use {@link Hoverfly}, so so long as your client respects these proxy settings you shouldn't have to configure it.</p>
 * <h2>Example Usage</h2>
 * <pre>
 * public class SomeTest {
 *      &#064;ClassRule
 *      public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(classpath("test-service.json"))
 *
 *      &#064;Test
 *      public void test() { //All requests will be proxied through Hoverfly
 *          // Given
 *          final RequestEntity<Void> bookFlightRequest = RequestEntity.delete(new URI("http://www.other-anotherService.com/api/bookings/1")).build();
 *
 *          // When
 *          final ResponseEntity<Void> bookFlightResponse = restTemplate.exchange(bookFlightRequest, Void.class);
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
 * <p>The recorded data will be saved in your src/test/resources directory</p>
 * <p><b>It's recommended to always use the {@link ClassRule} annotation, so you can share the same instance of Hoverfly through all your tests.</b>
 * This avoids the overhead of starting Hoverfly multiple times, and also helps ensure all your system properties are set before executing any other code.
 * If you want to change the data, you can do so in {@link Before} method by calling {@link HoverflyRule#simulate}, but this will not be thread safe.</p>
 *
 * @see SimulationSource
 * @see HoverflyDsl
 */
public class HoverflyRule extends ExternalResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(HoverflyRule.class);
    private final Hoverfly hoverfly;
    private final SimulationSource simulationSource;
    private final HoverflyMode hoverflyMode;
    private final Path capturePath;

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

    /**
     * Instantiates a rule which runs {@link Hoverfly} in capture mode
     *
     * @param recordedFilename the path to the recorded name relative to src/test/resources
     * @return the rule
     */
    public static HoverflyRule inCaptureMode(String recordedFilename) {
        return inCaptureMode(recordedFilename, configs());
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in capture mode
     *
     * @param recordedFilename the path to the recorded name relative to src/test/resources
     * @param hoverflyConfig   the config
     * @return the rule
     */
    public static HoverflyRule inCaptureMode(String recordedFilename, HoverflyConfig hoverflyConfig) {
        return new HoverflyRule(fileRelativeToTestResources(recordedFilename), hoverflyConfig);
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
        return inSimulationMode(SimulationSource.empty(), hoverflyConfig);
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

        if (hoverflyMode == SIMULATE && simulationSource != null) {
            hoverfly.importSimulation(simulationSource);
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
            hoverfly.stop();
        }
    }

    /**
     * Gets the proxy port this has run on, which could be useful when running {@link Hoverfly} on a random port.
     *
     * @return the proxy port
     */
    public int getProxyPort() {
        return hoverfly.getProxyPort();
    }


    /**
     * Changes the Simulation used by {@link Hoverfly}
     * @param simulationSource the simulation
     */
    public void simulate(SimulationSource simulationSource) {
        hoverfly.importSimulation(simulationSource);
    }

}
