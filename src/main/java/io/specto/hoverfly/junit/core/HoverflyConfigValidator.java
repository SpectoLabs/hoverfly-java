package io.specto.hoverfly.junit.core;

import java.io.IOException;
import java.net.ServerSocket;


class HoverflyConfigValidator {

    private static final int DEFAULT_PROXY_PORT = 8500;
    private static final int DEFAULT_ADMIN_PORT = 8888;

    HoverflyConfig validate(HoverflyConfig hoverflyConfig) {

        if (hoverflyConfig.getProxyPort() == 0) {
            hoverflyConfig.proxyPort(hoverflyConfig.isRemoteInstance() ? DEFAULT_PROXY_PORT : findUnusedPort());
        }

        if (hoverflyConfig.getAdminPort() == 0) {
            hoverflyConfig.adminPort(hoverflyConfig.isRemoteInstance() ? DEFAULT_ADMIN_PORT : findUnusedPort());
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
