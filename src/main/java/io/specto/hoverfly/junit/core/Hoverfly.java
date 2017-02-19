/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this classpath except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2016-2016 SpectoLabs Ltd.
 */
package io.specto.hoverfly.junit.core;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import io.specto.hoverfly.junit.core.model.Simulation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.sun.jersey.api.client.ClientResponse.Status.OK;
import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static io.specto.hoverfly.junit.core.HoverflyUtils.checkPortInUse;
import static io.specto.hoverfly.junit.core.HoverflyUtils.findUnusedPort;

/**
 * A wrapper class for the Hoverfly binary.  Manage the lifecycle of the processes, and then manage Hoverfly itself by using it's API endpoints.
 */
public class Hoverfly {

    private static final Logger LOGGER = LoggerFactory.getLogger(Hoverfly.class);
    private static final int BOOT_TIMEOUT_SECONDS = 10;
    private static final int RETRY_BACKOFF_INTERVAL_MS = 100;
    private static final String DEFAULT_HOVERFLY_URL = "http://localhost";
    private static final String HEALTH_CHECK_PATH = "/api/stats";
    private static final String SIMULATION_PATH = "/api/v2/simulation";
    private static final int DEFAULT_PROXY_PORT = 8500;
    private static final int DEFAULT_ADMIN_PORT = 8888;
    private final HoverflyConfig hoverflyConfig;
    private final HoverflyMode hoverflyMode;
    private final Integer proxyPort;
    private final Integer adminPort;
    private final WebResource hoverflyResource;
    private StartedProcess startedProcess;

    private SslConfigurer sslConfigurer = new SslConfigurer();
    private TempFileManager tempFileManager = new TempFileManager();
    private boolean useDefaultSslCert = true;

    /**
     * Instantiates {@link Hoverfly}
     *
     * @param hoverflyConfig the config
     * @param hoverflyMode   the mode
     */
    public Hoverfly(HoverflyConfig hoverflyConfig, HoverflyMode hoverflyMode) {
        this.hoverflyConfig = hoverflyConfig;
        this.hoverflyMode = hoverflyMode;

        String url = DEFAULT_HOVERFLY_URL;
        if (hoverflyConfig.isRemoteInstance()) {
            proxyPort = hoverflyConfig.getProxyPort() == 0 ? DEFAULT_PROXY_PORT : hoverflyConfig.getProxyPort();
            adminPort = hoverflyConfig.getAdminPort() == 0 ? DEFAULT_ADMIN_PORT : hoverflyConfig.getAdminPort();
            url = hoverflyConfig.getRemoteHost();
        } else {
            proxyPort = hoverflyConfig.getProxyPort() == 0 ? findUnusedPort() : hoverflyConfig.getProxyPort();
            adminPort = hoverflyConfig.getAdminPort() == 0 ? findUnusedPort() : hoverflyConfig.getAdminPort();
        }

        hoverflyResource = Client.create().resource(UriBuilder.fromUri(url).port(adminPort).build());
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

        if (!hoverflyConfig.isRemoteInstance()) {

            if (StringUtils.isNotBlank(hoverflyConfig.getSslCertificatePath()) && StringUtils.isNotBlank(hoverflyConfig.getSslKeyPath())) {
                useDefaultSslCert = false;
            }
            startHoverflyProcess();
        }

        waitForHoverflyToBecomeHealthy();

        if (useDefaultSslCert) {
            sslConfigurer.setTrustStore();
        }
        setProxySystemProperties();
    }

    private void startHoverflyProcess() {
        checkPortInUse(proxyPort);
        checkPortInUse(adminPort);

        Path binaryPath = tempFileManager.copyHoverflyBinary(new SystemConfigFactory().createSystemConfig());

        LOGGER.info("Executing binary at {}", binaryPath);
        final List<String> commands = new ArrayList<>();
        commands.add(binaryPath.toString());
        commands.add("-db");
        commands.add("memory");
        commands.add("-pp");
        commands.add(proxyPort.toString());
        commands.add("-ap");
        commands.add(adminPort.toString());

        if (hoverflyMode == HoverflyMode.CAPTURE) {
            commands.add("-capture");
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
     * Stops the running {@link Hoverfly} process
     */
    // TODO add shutdown hook for stop
    public void stop() {
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

        // TODO: clear system properties, and reset default SslContext?
        tempFileManager.purge();
    }

    // TODO: Extract HoverflyClient??
    /**
     * Imports a simulation into {@link Hoverfly} from a {@link SimulationSource}
     *
     * @param simulationSource the simulation to import
     */
    public void importSimulation(SimulationSource simulationSource) {
        LOGGER.info("Importing simulation data to Hoverfly");
        simulationSource.getSimulation().ifPresent(s ->
                hoverflyResource.path(SIMULATION_PATH)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .put(s));
    }


    /**
     * Exports a simulation and stores it on the filesystem at the given path
     *
     * @param path the path on the filesystem to where the simulation should be stored
     */
    public void exportSimulation(Path path) {
        LOGGER.info("Exporting simulation data from Hoverfly");
        try {
            Files.deleteIfExists(path);
            Files.write(path, hoverflyResource.path(SIMULATION_PATH).get(String.class).getBytes());
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
        return hoverflyResource.path(SIMULATION_PATH).get(Simulation.class);
    }

    /**
     * Gets the proxy port {@link Hoverfly} is running on
     *
     * @return the proxy port
     */
    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * Returns whether the running Hoverfly is healthy or not
     */
    private boolean isHealthy() {
        ClientResponse response = null;
        try {
            response = hoverflyResource.path(HEALTH_CHECK_PATH).get(ClientResponse.class);
            LOGGER.debug("Hoverfly health check status code is: {}", response.getStatus());
            return response.getStatus() == OK.getStatusCode();
        } catch (Exception e) {
            LOGGER.debug("Not yet healthy", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return false;
    }

    /**
     * Configures the JVM system properties to use Hoverfly as a proxy
     */
    private void setProxySystemProperties() {
        LOGGER.info("Setting proxy host to {}", "localhost");
        // TODO: There might be a bug here
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("https.proxyHost", "localhost");

        if (hoverflyConfig.isProxyLocalHost()) {
            System.setProperty("http.nonProxyHosts", "");
        } else {
            System.setProperty("http.nonProxyHosts", "local|*.local|169.254/16|*.169.254/16");
        }

        LOGGER.info("Setting proxy proxyPort to {}", hoverflyConfig.getProxyPort());

        System.setProperty("http.proxyPort", proxyPort.toString());
        System.setProperty("https.proxyPort", proxyPort.toString());
    }

    /**
     * Blocks until the Hoverfly process becomes healthy, otherwise time out
     */
    private void waitForHoverflyToBecomeHealthy() {
        final Instant now = Instant.now();

        while (Duration.between(now, Instant.now()).getSeconds() < BOOT_TIMEOUT_SECONDS) {
            if (isHealthy()) return;
            try {
                // TODO: prefer executors and tasks to threads
                Thread.sleep(RETRY_BACKOFF_INTERVAL_MS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException("Hoverfly has not become healthy in " + BOOT_TIMEOUT_SECONDS + " seconds");
    }

    public int getAdminPort() {
        return adminPort;
    }

}
