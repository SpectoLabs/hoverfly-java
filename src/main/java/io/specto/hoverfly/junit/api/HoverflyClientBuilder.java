package io.specto.hoverfly.junit.api;

import io.specto.hoverfly.junit.core.HoverflyConstants;

/**
 * HTTP client builder for Hoverfly admin API
 */
public class HoverflyClientBuilder {

    private String scheme = HoverflyConstants.HTTP;
    private String host = HoverflyConstants.LOCALHOST;
    private int port = HoverflyConstants.DEFAULT_ADMIN_PORT;
    private String authToken = null;

    public HoverflyClientBuilder scheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public HoverflyClientBuilder host(String host) {
        this.host = host;
        return this;
    }

    public HoverflyClientBuilder port(int port) {
        this.port = port;
        return this;
    }

    /**
     * Get token from environment variable "HOVERFLY_AUTH_TOKEN" to authenticate with admin API
     * @return this HoverflyClientBuilder for further customizations
     */
    public HoverflyClientBuilder withAuthToken() {
        this.authToken = System.getenv(HoverflyConstants.HOVERFLY_AUTH_TOKEN);
        return this;
    }

    public HoverflyClient build() {
        return new OkHttpHoverflyClient(scheme, host, port, authToken);
    }
}
