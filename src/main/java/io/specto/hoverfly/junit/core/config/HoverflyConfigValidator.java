package io.specto.hoverfly.junit.core.config;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyConfig;
import io.specto.hoverfly.junit.core.HoverflyConstants;
import java.net.URL;
import java.nio.file.Paths;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;


/**
 * Validate user-input {@link HoverflyConfig} before it is used by {@link Hoverfly}
 */
class HoverflyConfigValidator {


    /**
     * Sanity checking hoverfly configs and assign port number if necessary
     */
    HoverflyConfiguration validate(HoverflyConfiguration hoverflyConfig) {

        if (hoverflyConfig == null) {
            throw new IllegalArgumentException("HoverflyConfig cannot be null.");
        }

        // Validate remote config
        if (hoverflyConfig.isRemoteInstance()) {
            // Validate remote instance hostname
            if (hoverflyConfig.getHost() != null && hoverflyConfig.getHost().startsWith("http")) {
                try {
                    URI uri = new URI(hoverflyConfig.getHost());
                    hoverflyConfig.setHost(uri.getHost());
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("Remote hoverfly hostname is not valid: " + hoverflyConfig.getHost());
                }
            }
        }
        // Validate local config
        else {

            // Validate custom ca cert and key
            boolean isKeyBlank = StringUtils.isBlank(hoverflyConfig.getSslKeyPath());
            boolean isCertBlank = StringUtils.isBlank(hoverflyConfig.getSslCertificatePath());
            if (isKeyBlank && !isCertBlank || !isKeyBlank && isCertBlank) {
                throw new IllegalArgumentException("Both ca cert and key files are required to override the default Hoverfly ca cert.");
            }

            // Validate client auth cert and key
            boolean isClientKeyBlank = StringUtils.isBlank(hoverflyConfig.getClientKeyPath());
            boolean isClientCertBlank = StringUtils.isBlank(hoverflyConfig.getClientCertPath());
            if (isClientKeyBlank && !isClientCertBlank || !isClientKeyBlank && isClientCertBlank) {
                throw new IllegalArgumentException("Both client cert and key files are required to enable mutual TLS authentication.");
            }

            // Validate proxy port
            if (hoverflyConfig.getProxyPort() == 0) {
                hoverflyConfig.setProxyPort(findUnusedPort());
            }

            // Validate admin port
            if (hoverflyConfig.getAdminPort() == 0) {
                hoverflyConfig.setAdminPort(findUnusedPort());
            }
        }

        // Check proxy CA cert exists
        if (hoverflyConfig.getProxyCaCertificate().isPresent()) {
            checkResourceOnClasspath(hoverflyConfig.getProxyCaCertificate().get());
        }

        if (StringUtils.isBlank(hoverflyConfig.getResponseBodyFilesPath())) {
            // Set to test resources folder
            getTestResourcesFolderPath(HoverflyConstants.DEFAULT_HOVERFLY_RESOURCE_DIR)
                .ifPresent(hoverflyConfig::setResponseBodyFilesPath);
        } else if (hoverflyConfig.isRelativeResponseBodyFilesPath()) {
            String path = getTestResourcesFolderPath(hoverflyConfig.getResponseBodyFilesPath())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Response body files path not found: " + hoverflyConfig.getResponseBodyFilesPath()));
            hoverflyConfig.setResponseBodyFilesPath(path);
        }

        return hoverflyConfig;
    }


    /**
     * Looks for an unused port on the current machine
     */
    private static int findUnusedPort() {
        try (final ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Cannot find available port", e);
        }
    }

    private void checkResourceOnClasspath(String resourceName) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Optional.ofNullable(classLoader.getResource(resourceName))
                .orElseThrow(() -> new IllegalArgumentException("Resource not found with name: " + resourceName));
    }

    private Optional<String> getTestResourcesFolderPath(String relativePath) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(relativePath);
        return Optional.ofNullable(url).map(URL::getPath);
    }
}
