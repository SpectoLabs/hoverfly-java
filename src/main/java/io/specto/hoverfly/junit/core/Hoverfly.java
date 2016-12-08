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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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

    public void importSimulation(URI serviceDataURI) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(String.format(SIMULATION_URL, adminPort)).openConnection();
        con.setRequestMethod("PUT");
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            if (serviceDataURI.getScheme().startsWith("http")) {
                IOUtils.copy(serviceDataURI.toURL().openStream(), os);
            } else {
                Files.copy(Paths.get(serviceDataURI), os);
            }
            os.flush();
        }
        con.connect();
        int responseCode = con.getResponseCode();
        if (responseCode < 200 || responseCode > 299) {

            String result = new BufferedReader(new InputStreamReader(con.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));
            throw new RuntimeException("Submit simulation data failed with error: " + result);
        }
    }

    public void exportSimulation(URI serviceDataURI) {
        LOGGER.info("Storing captured data");
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(String.format(SIMULATION_URL, adminPort)).openConnection();
            con.setRequestMethod("GET");
            final Path path = Paths.get(serviceDataURI);
            Files.deleteIfExists(path);
            Files.copy(con.getInputStream(), path);
        } catch (IOException e) {
            throw new RuntimeException("Unable to persist captured data", e);
        }
    }

    private boolean isHealthy(int adminPort) {
        boolean isHealthy = false;
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(String.format(HEALTH_CHECK_URL, adminPort)).openConnection();
            con.setRequestMethod("GET");
            LOGGER.debug("Hoverfly health check status code is: {}", con.getResponseCode());
            isHealthy = con.getResponseCode() == 200;
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
            if (isHealthy(adminPort)) return;
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
