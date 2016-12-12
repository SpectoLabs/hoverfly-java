package io.specto.hoverfly.webserver;

import java.io.IOException;
import java.net.ServerSocket;

class WebServerUtils {

    static int findUnusedPort() {
        try (final ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Cannot find available port", e);
        }
    }
}
