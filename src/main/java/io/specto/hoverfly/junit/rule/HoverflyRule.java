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

import io.specto.hoverfly.junit.core.*;
import io.specto.hoverfly.junit.core.model.Simulation;
import io.specto.hoverfly.junit.dsl.HoverflyDsl;
import io.specto.hoverfly.junit.dsl.RequestMatcherBuilder;
import io.specto.hoverfly.junit.dsl.StubServiceBuilder;
import io.specto.hoverfly.junit.verification.VerificationCriteria;
import org.apache.commons.lang3.StringUtils;
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
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

import static io.specto.hoverfly.junit.core.HoverflyConfig.localConfigs;
import static io.specto.hoverfly.junit.core.HoverflyConstants.DEFAULT_HOVERFLY_EXPORT_PATH;
import static io.specto.hoverfly.junit.core.HoverflyMode.*;
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

    private final Hoverfly hoverfly;
    private final HoverflyMode hoverflyMode;
    private Path capturePath;
    private List<SimulationSource> simulationSources = new ArrayList<>();
    private boolean enableSimulationPrint;

    private HoverflyRule(HoverflyMode hoverflyMode, final SimulationSource simulationSource, final HoverflyConfig hoverflyConfig) {
        this.hoverflyMode = hoverflyMode;
        this.hoverfly = new Hoverfly(hoverflyConfig, hoverflyMode);
        if (simulationSource != null) {
            this.simulationSources.add(simulationSource);
        }
    }

    private HoverflyRule(final Path capturePath, final HoverflyConfig hoverflyConfig) {
        this.hoverflyMode = CAPTURE;
        this.hoverfly = new Hoverfly(hoverflyConfig, hoverflyMode);
        this.capturePath = capturePath;
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in capture mode if
     * recorded file is not present, or in simulation mode if record file is present
     * @param recordFile the path where captured or simulated traffic is taken. Relative to src/test/resources/hoverfly
     * @return the rule
     */
    public static HoverflyRule inCaptureOrSimulationMode(String recordFile) {
        return inCaptureOrSimulationMode(recordFile, localConfigs());
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in capture mode if
     * recorded file is not present, or in simulation mode if record file is present
     * @param recordFile     the path where captured or simulated traffic is taken. Relative to src/test/resources/hoverfly
     * @param hoverflyConfig the config
     * @return the rule
     */
    public static HoverflyRule inCaptureOrSimulationMode(String recordFile, HoverflyConfig hoverflyConfig) {
        Path path = fileRelativeToTestResourcesHoverfly(recordFile);
        if (Files.isReadable(path)) {
            return inSimulationMode(file(path), hoverflyConfig);
        } else {
            return inCaptureMode(recordFile, hoverflyConfig);
        }
    }

    public static HoverflyRule inCaptureMode() {
        return inCaptureMode(localConfigs());
    }

    public static HoverflyRule inCaptureMode(HoverflyConfig hoverflyConfig) {
        return new HoverflyRule(null, hoverflyConfig);
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in capture mode
     * @param outputFilename the output simulation file name relative to src/test/resources/hoverfly
     * @return the rule
     */
    public static HoverflyRule inCaptureMode(String outputFilename) {
        return inCaptureMode(outputFilename, localConfigs());
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in capture mode
     * @param outputFilename the output simulation file name  relative to src/test/resources/hoverfly
     * @param hoverflyConfig the config
     * @return the rule
     */
    public static HoverflyRule inCaptureMode(String outputFilename, HoverflyConfig hoverflyConfig) {
        return inCaptureMode(DEFAULT_HOVERFLY_EXPORT_PATH, outputFilename, hoverflyConfig);
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in capture mode
     * @param outputDir the directory path relative to your project root for exporting the simulation file
     * @param outputFilename the output simulation file name
     * @return the rule
     */
    public static HoverflyRule inCaptureMode(String outputDir, String outputFilename) {
        return inCaptureMode(outputDir, outputFilename, localConfigs());
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in capture mode
     * @param outputDir the directory path relative to your project root for exporting the simulation file
     * @param outputFilename the output simulation file name
     * @param hoverflyConfig the config
     * @return the rule
     */
    public static HoverflyRule inCaptureMode(String outputDir, String outputFilename, HoverflyConfig hoverflyConfig) {
        if (StringUtils.isBlank(outputFilename)) {
            throw new IllegalArgumentException("Output simulation file name can not be blank.");
        }
        Path exportPath = createDirectoryIfNotExist(outputDir);
        return new HoverflyRule(exportPath.resolve(outputFilename), hoverflyConfig);
    }


    /**
     * Instantiates a rule which runs {@link Hoverfly} in simulate mode with no data
     * @return the rule
     */
    public static HoverflyRule inSimulationMode() {
        return inSimulationMode(localConfigs());
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in simulate mode with no data
     * @param hoverflyConfig the config
     * @return the rule
     */
    public static HoverflyRule inSimulationMode(final HoverflyConfig hoverflyConfig) {
        return inSimulationMode(null, hoverflyConfig);
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in simulate mode
     * @param simulationSource the simulation to import
     * @return the rule
     */
    public static HoverflyRule inSimulationMode(final SimulationSource simulationSource) {
        return inSimulationMode(simulationSource, localConfigs());
    }

    public static HoverflyRule inSimulationMode(final SimulationSource simulationSource, final HoverflyConfig hoverflyConfig) {
        return new HoverflyRule(SIMULATE, simulationSource, hoverflyConfig);
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in spy mode with no data
     * @return the rule
     */
    public static HoverflyRule inSpyMode() {
        return inSpyMode(localConfigs());
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in spy mode with no data
     * @param hoverflyConfig the config
     * @return the rule
     */
    public static HoverflyRule inSpyMode(final HoverflyConfig hoverflyConfig) {
        return inSpyMode(null, hoverflyConfig);
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in spy mode
     * @param simulationSource the simulation to import
     * @return the rule
     */
    public static HoverflyRule inSpyMode(final SimulationSource simulationSource) {
        return inSpyMode(simulationSource, localConfigs());
    }

    public static HoverflyRule inSpyMode(final SimulationSource simulationSource, final HoverflyConfig hoverflyConfig) {
        return new HoverflyRule(SPY, simulationSource, hoverflyConfig);
    }


    /**
     * Instantiates a rule which runs {@link Hoverfly} in diff mode with no data
     * @return the rule
     */
    public static HoverflyRule inDiffMode() {
        return inDiffMode(localConfigs());
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in diff mode with no data
     * @param hoverflyConfig the config
     * @return the rule
     */
    public static HoverflyRule inDiffMode(final HoverflyConfig hoverflyConfig) {
        return inDiffMode(null, hoverflyConfig);
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in diff mode
     * @param simulationSource the simulation to import the responses will be compared to
     * @return the rule
     */
    public static HoverflyRule inDiffMode(final SimulationSource simulationSource) {
        return inDiffMode(simulationSource, localConfigs());
    }

    /**
     * Instantiates a rule which runs {@link Hoverfly} in diff mode
     * @param simulationSource the simulation to import the responses will be compared to
     * @param hoverflyConfig the config
     * @return the rule
     */
    public static HoverflyRule inDiffMode(final SimulationSource simulationSource, final HoverflyConfig hoverflyConfig) {
        return new HoverflyRule(DIFF, simulationSource, hoverflyConfig);
    }

    /**
     * Log warning if {@link HoverflyRule} is annotated with {@link Rule}
     */
    @Override
    public Statement apply(Statement base, Description description) {
        if (isAnnotatedWithRule(description)) {
            LOGGER.warn("It is recommended to use HoverflyRule with @ClassRule to get better performance in your tests, " +
                    "and prevent known issue with Apache HttpClient. For more information, " +
                    "please see http://hoverfly-java.readthedocs.io/en/latest/pages/misc/misc.html#apache-httpclient.");
        }
        return super.apply(base, description);
    }

    /**
     * Starts an instance of Hoverfly
     */
    @Override
    protected void before() {
        hoverfly.start();

        if (hoverflyMode.allowSimulationImport()) {
            importSimulation();
        }

        if (hoverfly.getHoverflyConfig().isIncrementalCapture() && this.capturePath != null && Files.isReadable(this.capturePath)) {
            hoverfly.simulate(SimulationSource.file(this.capturePath));
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

    public SslConfigurer getSslConfigurer() {
        return hoverfly.getSslConfigurer();
    }
    /**
     * Gets started Hoverfly mode
     *
     * @return the mode.
     */
    public HoverflyMode getHoverflyMode() {
        return hoverflyMode;
    }

    // TODO add another simulate method that appends add new sources to the initial simulation source
    /**
     * Changes the Simulation used by {@link Hoverfly}
     * It also reset the journal to ensure verification can be done on the new simulation source.
     *
     * @param simulationSource the simulation
     */
    public void simulate(SimulationSource simulationSource, SimulationSource... sources) {
        checkMode(HoverflyMode::allowSimulationImport);
        this.simulationSources = new ArrayList<>();
        this.simulationSources.add(simulationSource);
        if (sources.length > 0) {
            this.simulationSources.addAll(Arrays.asList(sources));
        }
        hoverfly.resetState();
        importSimulation();
        hoverfly.resetJournal();
    }

    /**
     * Stores what's currently been captured in the currently assigned file, reset simulations and journal logs, then starts capture again
     * ready to store in the new file once complete.
     * @param outputFilename the output simulation file name relative to src/test/resources/hoverfly
     */
    public void capture(final String outputFilename) {
        capture(DEFAULT_HOVERFLY_EXPORT_PATH, outputFilename);
    }

    /**
     * Stores what's currently been captured in the currently assigned file, reset simulations and journal logs, then starts capture again
     * ready to store in the new file once complete.
     * @param outputDir the directory path relative to your project root for exporting the simulation file
     * @param outputFilename the output simulation file name relative to src/test/resources/hoverfly
     */
    public void capture(final String outputDir, final String outputFilename) {
        checkMode(mode -> mode == CAPTURE);
        if (capturePath != null) {
            hoverfly.exportSimulation(capturePath);
        }
        hoverfly.reset();
        capturePath = Paths.get(outputDir).resolve(outputFilename);
    }


    /**
     * Get custom Hoverfly header name used by Http client to authenticate with secured Hoverfly proxy
     * @return the custom Hoverfly authorization header name
     */
    @Deprecated
    public String getAuthHeaderName() {
        return HoverflyConstants.X_HOVERFLY_AUTHORIZATION;
    }

    /**
     * Get Bearer token used by Http client to authenticate with secured Hoverfly proxy
     * @return a custom Hoverfly authorization header value
     */
    @Deprecated
    public String getAuthHeaderValue() {
        Optional<String> authToken = hoverfly.getHoverflyConfig().getAuthToken();
        return authToken.map(s -> "Bearer " + s).orElse(null);
    }

    /**
     * Print the simulation data to console for debugging purpose. This can be set when you are building the HoverflyRule
     * @return this HoverflyRule
     */
    public HoverflyRule printSimulationData() {
        enableSimulationPrint = true;
        return this;
    }

    public void verify(RequestMatcherBuilder requestMatcher) {
        hoverfly.verify(requestMatcher);
    }

    public void verify(RequestMatcherBuilder requestMatcher, VerificationCriteria criteria) {
        hoverfly.verify(requestMatcher, criteria);
    }

    public void verifyZeroRequestTo(StubServiceBuilder requestedServiceBuilder) {
        hoverfly.verifyZeroRequestTo(requestedServiceBuilder);
    }

    public void verifyAll() {
        hoverfly.verifyAll();
    }

    public void resetJournal() {
        hoverfly.resetJournal();
    }

    /**
     * Deletes all state from Hoverfly
     */
    public void resetState() {
        hoverfly.resetState();
    }

    /**
     * Get all state from Hoverfly
     *
     * @return the state
     */
    public Map<String, String> getState() {
        return hoverfly.getState();
    }

    /**
     * Deletes all state from Hoverfly and then sets the state.
     *
     * @param state the new state
     */
    public void setState(final Map<String, String> state) {
        hoverfly.setState(state);
    }

    /**
     *  Updates state in Hoverfly.
     *
     *  @param state the state to update with
     */
    public void updateState(final Map<String, String> state) {
        hoverfly.updateState(state);
    }

    public void resetDiffs() {
        hoverfly.resetDiffs();
    }

    /**
     * Asserts that there was no diff between any of the expected responses set by simulations and the actual responses
     * returned from the real service. When the assertion is done then all available diffs are removed from Hoverfly.
     */
    public void assertThatNoDiffIsReported() {
        assertThatNoDiffIsReported(true);
    }

    /**
     * Asserts that there was no diff between any of the expected responses set by simulations and the actual responses
     * returned from the real service.
     * The parameter {@code shouldResetDiff} says if all available diffs should be removed when the assertion is done.
     *
     * @param shouldResetDiff if all available diffs should be removed when the assertion is done.
     */
    public void assertThatNoDiffIsReported(boolean shouldResetDiff) {
        hoverfly.assertThatNoDiffIsReported(shouldResetDiff);
    }

    private void checkMode(Predicate<HoverflyMode> condition) {
        if (!condition.test(hoverflyMode)) {
            throw new HoverflyRuleException(hoverflyMode.name() + " mode does not support this operation.");
        }
    }

    private void importSimulation() {
        if (simulationSources != null && !simulationSources.isEmpty()) {

            if (simulationSources.size() == 1) {
                hoverfly.simulate(simulationSources.get(0));
            } else {

                SimulationSource[] sources = new SimulationSource[simulationSources.size() - 1];
                sources = simulationSources.subList(1, simulationSources.size()).toArray(sources);
                hoverfly.simulate(simulationSources.get(0), sources);
            }

            if (enableSimulationPrint) {
                Simulation imported = hoverfly.getSimulation();
                prettyPrintSimulation(imported);
            }
        }
    }

    static class HoverflyRuleException extends RuntimeException {

        HoverflyRuleException(String message) {
            super(message);
        }
    }
}
