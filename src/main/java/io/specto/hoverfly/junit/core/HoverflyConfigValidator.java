package io.specto.hoverfly.junit.core;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.ServerSocket;


class HoverflyConfigValidator {

    private static final int DEFAULT_PROXY_PORT = 8500;
    private static final int DEFAULT_ADMIN_PORT = 8888;

    /**
     * Sanity checking hoverfly configs and assign port number if necessary
     */
    HoverflyConfig validate(HoverflyConfig hoverflyConfig) {

        if (hoverflyConfig.getProxyPort() == 0) {
            hoverflyConfig.proxyPort(hoverflyConfig.isRemoteInstance() ? DEFAULT_PROXY_PORT : findUnusedPort());
        }

        if (hoverflyConfig.getAdminPort() == 0) {
            hoverflyConfig.adminPort(hoverflyConfig.isRemoteInstance() ? DEFAULT_ADMIN_PORT : findUnusedPort());
        }

        boolean isKeyBlank = StringUtils.isBlank(hoverflyConfig.getSslKeyPath());
        boolean isCertBlank = StringUtils.isBlank(hoverflyConfig.getSslCertificatePath());
        if (isKeyBlank && !isCertBlank || !isKeyBlank && isCertBlank) {
            throw new IllegalArgumentException("Both SSL key and certificate files are required to override the default Hoverfly SSL.");
        }

        if (hoverflyConfig.isRemoteInstance() && !isKeyBlank && !isCertBlank) {
            throw new IllegalArgumentException("Attempt to configure SSL on remote instance is prohibited.");
        }

        // TODO validate supported SSL file format

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
