package io.specto.hoverfly.junit.core;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


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
                throw new IllegalArgumentException("Both SSL key and certificate files are required to override the default Hoverfly SSL.");
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
            HoverflyUtils.findResourceOnClasspath(hoverflyConfig.getProxyCaCertificate().get());
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
}
