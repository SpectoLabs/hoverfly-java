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
package io.specto.hoverfly.junit.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.specto.hoverfly.junit.core.model.HoverflyInfo;
import io.specto.hoverfly.junit.core.model.Simulation;
import io.specto.hoverfly.junit.api.HoverflyClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static io.specto.hoverfly.junit.core.HoverflyUtils.checkPortInUse;

/**
 * A wrapper class for the Hoverfly binary.  Manage the lifecycle of the processes, and then manage Hoverfly itself by using it's API endpoints.
 */
// TODO extract interface and create LocalHoverfly and RemoteHoverfly
public class Hoverfly implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Hoverfly.class);
    private static final ObjectWriter JSON_PRETTY_PRINTER = new ObjectMapper().writerWithDefaultPrettyPrinter();
    private static final int BOOT_TIMEOUT_SECONDS = 10;
    private static final int RETRY_BACKOFF_INTERVAL_MS = 100;

    private final HoverflyConfiguration hoverflyConfig;
    private final HoverflyMode hoverflyMode;
    private final ProxyConfigurer proxyConfigurer;
    private final SslConfigurer sslConfigurer = new SslConfigurer();
    private final HoverflyClient hoverflyClient;

    private final TempFileManager tempFileManager = new TempFileManager();
    private StartedProcess startedProcess;
    private boolean useDefaultSslCert = true;

    /**
     * Instantiates {@link Hoverfly}
     *
     * @param hoverflyConfigBuilder the config
     * @param hoverflyMode   the mode
     */
    public Hoverfly(HoverflyConfig hoverflyConfigBuilder, HoverflyMode hoverflyMode) {
        hoverflyConfig = hoverflyConfigBuilder.build();
        this.proxyConfigurer = new ProxyConfigurer(hoverflyConfig);
        this.hoverflyClient = HoverflyClient.custom()
                .scheme(hoverflyConfig.getScheme())
                .host(hoverflyConfig.getHost())
                .port(hoverflyConfig.getAdminPort())
                .withAuthToken()
                .build();
        this.hoverflyMode = hoverflyMode;
    }

    /**
     * Instantiates {@link Hoverfly}
     *
     * @param hoverflyMode the mode
     */
    public Hoverfly(HoverflyMode hoverflyMode) {
        this(configs(), hoverflyMode);
    }

    /**
     * <ol>
     * <li>Adds Hoverfly SSL certificate to the trust store</li>
     * <li>Sets the proxy system properties to route through Hoverfly</li>
     * <li>Starts Hoverfly</li>
     * </ol>
     */
    public void start() {

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));

        if (startedProcess != null) {
            LOGGER.warn("Local Hoverfly is already running.");
            return;
        }

        if (!hoverflyConfig.isRemoteInstance()) {
            startHoverflyProcess();
        }

        waitForHoverflyToBecomeHealthy();

        if (StringUtils.isNotBlank(hoverflyConfig.getDestination())) {
            setDestination(hoverflyConfig.getDestination());
        }

        setMode(hoverflyMode);

        if (hoverflyConfig.getProxyCaCertificate().isPresent()) {
          sslConfigurer.setDefaultSslContext(hoverflyConfig.getProxyCaCertificate().get());
        } else if (useDefaultSslCert) {
            sslConfigurer.setDefaultSslContext();
        }

        proxyConfigurer.setProxySystemProperties();
    }

    private void startHoverflyProcess() {
        checkPortInUse(hoverflyConfig.getProxyPort());
        checkPortInUse(hoverflyConfig.getAdminPort());

        final SystemConfig systemConfig = new SystemConfigFactory().createSystemConfig();

        Path binaryPath = tempFileManager.copyHoverflyBinary(systemConfig);

        LOGGER.info("Executing binary at {}", binaryPath);
        final List<String> commands = new ArrayList<>();
        commands.add(binaryPath.toString());
        commands.add("-db");
        commands.add("memory");
        commands.add("-pp");
        commands.add(String.valueOf(hoverflyConfig.getProxyPort()));
        commands.add("-ap");
        commands.add(String.valueOf(hoverflyConfig.getAdminPort()));

        if (StringUtils.isNotBlank(hoverflyConfig.getSslCertificatePath())) {
            tempFileManager.copyClassPathResource(hoverflyConfig.getSslCertificatePath(), "ca.crt");
            commands.add("-cert");
            commands.add("ca.crt");
        }
        if (StringUtils.isNotBlank(hoverflyConfig.getSslKeyPath())) {
            tempFileManager.copyClassPathResource(hoverflyConfig.getSslKeyPath(), "ca.key");
            commands.add("-key");
            commands.add("ca.key");
            useDefaultSslCert = false;
        }

        try {
            startedProcess = new ProcessExecutor()
                    .command(commands)
                    .redirectOutput(Slf4jStream.of(LOGGER).asInfo())
                    .directory(tempFileManager.getTempDirectory().toFile())
                    .start();
        } catch (IOException e) {
            throw new IllegalStateException("Could not start Hoverfly process", e);
        }
    }

    /**
     * Stops the running {@link Hoverfly} process and clean up resources
     */
    @Override
    public void close() {
        cleanUp();
    }

    /**
     * Imports a simulation into {@link Hoverfly} from a {@link SimulationSource}
     *
     * @param simulationSource the simulation to import
     */
    public void importSimulation(SimulationSource simulationSource) {
        LOGGER.info("Importing simulation data to Hoverfly");

        final Simulation simulation = simulationSource.getSimulation();

        hoverflyClient.setSimulation(simulation);
    }

    /**
     * Clears Hoverfly instance in case of running Hoverfly in standalone.
     */
    // TODO where is it used?
    public void reset() {
        importSimulation(SimulationSource.empty());
    }

    /**
     * Exports a simulation and stores it on the filesystem at the given path
     *
     * @param path the path on the filesystem to where the simulation should be stored
     */
    public void exportSimulation(Path path) {

        if (path == null) return;

        LOGGER.info("Exporting simulation data from Hoverfly");
        try {
            Files.deleteIfExists(path);
            final Simulation simulation = hoverflyClient.getSimulation();
            persistSimulation(path, simulation);
        } catch (Exception e) {
            LOGGER.error("Failed to export simulation data", e);
        }
    }

    /**
     * Gets the simulation currently used by the running {@link Hoverfly} instance
     *
     * @return the simulation
     */
    public Simulation getSimulation() {
        return hoverflyClient.getSimulation();
    }

    /**
     * Gets configuration information from the running instance of Hoverfly.
     * @return the hoverfly info object
     */
    public HoverflyInfo getHoverflyInfo() {
        return hoverflyClient.getConfigInfo();
    }

    /**
     * Sets a new destination for the running instance of Hoverfly, overwriting the existing destination setting.
     * @param destination the destination setting to override
     */
    public void setDestination(String destination) {
        hoverflyClient.setDestination(destination);
    }


    /**
     * Changes the mode of the running instance of Hoverfly.
     * @param mode hoverfly mode to change
     */
    public void setMode(HoverflyMode mode) {
        hoverflyClient.setMode(mode);
    }

    /**
     * Gets the validated {@link HoverflyConfig} object used by the current Hoverfly instance
     * @return the current Hoverfly configurations
     */
    public HoverflyConfiguration getHoverflyConfig() {
        return hoverflyConfig;
    }

    /**
     * Gets the currently activated Hoverfly mode
     * @return hoverfly mode
     */
    public HoverflyMode getMode() {
        return HoverflyMode.valueOf(hoverflyClient.getConfigInfo().getMode().toUpperCase());
    }

    private void persistSimulation(Path path, Simulation simulation) throws IOException {
        Files.createDirectories(path.getParent());
        JSON_PRETTY_PRINTER.writeValue(path.toFile(), simulation);
    }


    /**
     * Blocks until the Hoverfly process becomes healthy, otherwise time out
     */
    private void waitForHoverflyToBecomeHealthy() {
        final Instant now = Instant.now();

        while (Duration.between(now, Instant.now()).getSeconds() < BOOT_TIMEOUT_SECONDS) {
            if (hoverflyClient.getHealth()) return;
            try {
                // TODO: prefer executors and tasks to threads
                Thread.sleep(RETRY_BACKOFF_INTERVAL_MS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException("Hoverfly has not become healthy in " + BOOT_TIMEOUT_SECONDS + " seconds");
    }

    private void cleanUp() {
        LOGGER.info("Destroying hoverfly process");

        if (startedProcess != null) {
            Process process = startedProcess.getProcess();
            process.destroy();

            // Some platforms terminate process asynchronously, eg. Windows, and cannot guarantee that synchronous file deletion
            // can acquire file lock
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<Integer> future = executorService.submit((Callable<Integer>) process::waitFor);
            try {
                future.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.warn("Timeout when waiting for hoverfly process to terminate.");
            }
            executorService.shutdownNow();
        }

        proxyConfigurer.restoreProxySystemProperties();
        // TODO: reset default SslContext?
        tempFileManager.purge();
    }


}
