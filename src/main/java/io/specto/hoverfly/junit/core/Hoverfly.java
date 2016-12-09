/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.specto.hoverfly.junit.core.model.Simulation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.util.Arrays.asList;

public class Hoverfly {

    private static final Logger LOGGER = LoggerFactory.getLogger(Hoverfly.class);
    private static final int BOOT_TIMEOUT_SECONDS = 10;
    private static final String HEALTH_CHECK_URL = "http://localhost:%s/api/stats";
    private static final String SIMULATION_URL = "http://localhost:%s/api/v2/simulation";
    private static final String BINARY_PATH = "hoverfly_%s_%s";
    private final HoverflyConfig hoverflyConfig;
    private final HoverflyMode hoverflyMode;
    private final Integer proxyPort;
    private final Integer adminPort;
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private StartedProcess startedProcess;
    private Path binaryPath;


    public Hoverfly(HoverflyConfig hoverflyConfig, HoverflyMode hoverflyMode) {
        this.hoverflyConfig = hoverflyConfig;
        this.hoverflyMode = hoverflyMode;
        proxyPort = hoverflyConfig.getProxyPort() == 0 ? HoverflyUtils.findUnusedPort() : hoverflyConfig.getProxyPort();
        adminPort = hoverflyConfig.getAdminPort() == 0 ? HoverflyUtils.findUnusedPort() : hoverflyConfig.getAdminPort();
    }

    public void start() throws IOException, URISyntaxException {

        setTrustStore();

        setProxySystemProperties();

        final String binaryName = String.format(BINARY_PATH, HoverflyUtils.getOs(), HoverflyUtils.getArchitectureType()) + (SystemUtils.IS_OS_WINDOWS ? ".exe" : "");
        LOGGER.info("Selecting the following binary based on the current operating system: {}", binaryName);
        binaryPath = extractBinary(binaryName);

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

        startedProcess = new ProcessExecutor()
                .command(commands)
                .redirectOutput(Slf4jStream.of(LOGGER).asInfo())
                .directory(binaryPath.getParent().toFile())
                .start();

        waitForHoverflyToStart();
    }

    public void stop() {
        LOGGER.info("Destroying hoverfly process");
        startedProcess.getProcess().destroy();

        try {
            Files.deleteIfExists(binaryPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete hoverfly binary", e);
        }
    }

    public void importSimulation(Simulation simulation) {
        try {
            HttpEntity entity = EntityBuilder.create()
                    .setText(new ObjectMapper().writeValueAsString(simulation))
                    .build();
            putSimulation(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void importSimulation(URI simulationDataUri) {

        try {
            EntityBuilder entityBuilder = EntityBuilder.create();

            if (simulationDataUri.getScheme().startsWith("http")) {
                entityBuilder.setStream(simulationDataUri.toURL().openStream());
            } else {
                entityBuilder.setFile(Paths.get(simulationDataUri).toFile());
            }

            HttpEntity httpEntity = entityBuilder.build();
            putSimulation(httpEntity);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Simulation getSimulationData() {
        try (InputStream content = getSimulation()) {
            return new ObjectMapper().readValue(content, Simulation.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void exportSimulation(URI serviceDataURI) {
        LOGGER.info("Storing captured Data");
        try (InputStream content = getSimulation()){
            final Path path = Paths.get(serviceDataURI);
            Files.deleteIfExists(path);
            Files.copy(content, path);
        } catch (IOException e) {
            throw new RuntimeException("Unable to persist captured Data", e);
        }
    }

    private InputStream getSimulation() throws IOException {
        HttpUriRequest getRequest = new HttpGet(String.format(SIMULATION_URL, adminPort));
        HttpResponse response = httpClient.execute(getRequest);
        return response.getEntity().getContent();
    }

    private void putSimulation(HttpEntity entity) {
        HttpUriRequest putRequest = RequestBuilder.put(String.format(SIMULATION_URL, adminPort))
                .setEntity(entity)
                .setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .build();
        try (CloseableHttpResponse response = httpClient.execute(putRequest)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < 200 || statusCode > 299) {
                throw new RuntimeException("Submit simulation data failed with error: " + EntityUtils.toString(response.getEntity()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isHealthy() {
        boolean isHealthy = false;
        HttpUriRequest getRequest = new HttpGet(String.format(HEALTH_CHECK_URL, adminPort));
        try (CloseableHttpResponse response = httpClient.execute(getRequest)) {
            int statusCode = response.getStatusLine().getStatusCode();
            isHealthy = statusCode == 200;
            LOGGER.debug("Hoverfly health check status code is: {}", statusCode);
        } catch (IOException e) {
            LOGGER.debug("Exception curling health check", e);
        }
        return isHealthy;
    }

    private void setProxySystemProperties() {
        LOGGER.info("Setting proxy host to {}", "localhost");
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

    public Integer getProxyPort() {
        return proxyPort;
    }

    private void waitForHoverflyToStart() {
        final Instant now = Instant.now();

        while (Duration.between(now, Instant.now()).getSeconds() < BOOT_TIMEOUT_SECONDS) {
            if (isHealthy()) return;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException("Hoverfly has not become healthy in " + BOOT_TIMEOUT_SECONDS + " seconds");
    }

    private Path extractBinary(final String binaryName) throws IOException {
        final URI sourceHoverflyUrl = HoverflyUtils.findResourceOnClasspath("binaries/" + binaryName);
        final Path temporaryHoverflyPath = Files.createTempFile(binaryName, "");
        LOGGER.info("Storing binary in temporary directory {}", temporaryHoverflyPath);
        final File temporaryHoverflyFile = temporaryHoverflyPath.toFile();
        FileUtils.copyURLToFile(sourceHoverflyUrl.toURL(), temporaryHoverflyFile);
        if (SystemUtils.IS_OS_WINDOWS) {
            temporaryHoverflyFile.setExecutable(true);
            temporaryHoverflyFile.setReadable(true);
            temporaryHoverflyFile.setWritable(true);
        } else {
            Files.setPosixFilePermissions(temporaryHoverflyPath, new HashSet<>(asList(OWNER_EXECUTE, OWNER_READ)));
        }

        return temporaryHoverflyPath;
    }

    private void setTrustStore() {
        try {
            // load your key store as a stream and initialize a KeyStore
            InputStream trustStream = HoverflyUtils.findResourceOnClasspath("hoverfly.jks").toURL().openStream();

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

            // load the stream to your store
            trustStore.load(trustStream, "hoverfly".toCharArray());

            // initialize a trust manager factory with the trusted store
            TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustFactory.init(trustStore);

            // get the trust managers from the factory
            TrustManager[] trustManagers = trustFactory.getTrustManagers();

            // initialize an ssl context to use these managers and set as default
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagers, null);
            SSLContext.setDefault(sslContext);
        } catch (Exception e) {
            throw new RuntimeException("Unable to set hoverfly trust store", e);
        }
    }
}
