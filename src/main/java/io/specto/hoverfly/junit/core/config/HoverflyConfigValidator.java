package io.specto.hoverfly.junit.core.config;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyConfig;
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
                throw new IllegalArgumentException("Both SSL key and certificate files are required to override the default Hoverfly SSL.");
            }

        }

        // Check proxy CA cert exists
        if (hoverflyConfig.getProxyCaCertificate().isPresent()) {
            checkResourceOnClasspath(hoverflyConfig.getProxyCaCertificate().get());
        }

        return hoverflyConfig;
    }

    private void checkResourceOnClasspath(String resourceName) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Optional.ofNullable(classLoader.getResource(resourceName))
                .orElseThrow(() -> new IllegalArgumentException("Resource not found with name: " + resourceName));
    }
}
