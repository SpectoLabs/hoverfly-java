package io.specto.hoverfly.junit.core.config;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyConfig;
import io.specto.hoverfly.junit.core.HoverflyConstants;

import static io.specto.hoverfly.junit.core.HoverflyConstants.DEFAULT_ADMIN_PORT;
import static io.specto.hoverfly.junit.core.HoverflyConstants.DEFAULT_PROXY_PORT;

/**
 * Config builder interface for settings specific to external/remote {@link Hoverfly} instance
 */
public class RemoteHoverflyConfig extends HoverflyConfig {

    private String host;
    private String scheme;
    private String authToken;
    private String adminCertificate; // file name relative to test resources folder


    /**
     * Sets hostname for the external Hoverfly, default to localhost
     * @param host the hostname
     * @return the {@link RemoteHoverflyConfig} for further customizations
     */
    public RemoteHoverflyConfig host(String host) {
        this.host = host;
        return this;
    }

    /**
     * Sets up custom authentication header for secured remote Hoverfly instance.
     * Gets auth token from an environment variable "HOVERFLY_AUTH_TOKEN"
     *
     * @return the {@link RemoteHoverflyConfig} for further customizations
     */
    public RemoteHoverflyConfig withAuthHeader() {
        this.authToken = System.getenv(HoverflyConstants.HOVERFLY_AUTH_TOKEN);
        return this;
    }

    /**
     * Sets up custom authentication header for secured remote Hoverfly instance
     * @param authToken a token for Hoverfly authentication
     * @return the {@link RemoteHoverflyConfig} for further customizations
     */
    public RemoteHoverflyConfig withAuthHeader(String authToken) {
        this.authToken = authToken;
        return this;
    }

    /**
     * Sets up to use admin endpoint over HTTPS
     * @return the {@link RemoteHoverflyConfig} for further customizations
     */
    public RemoteHoverflyConfig withHttpsAdminEndpoint() {
        this.scheme = HoverflyConstants.HTTPS;
        this.adminPort = HoverflyConstants.DEFAULT_HTTPS_ADMIN_PORT;
        return this;
    }

    // TODO add support for custom server certificate for admin endpoint

    @Override
    public HoverflyConfiguration build() {
        if (adminPort == 0) {
            adminPort = DEFAULT_ADMIN_PORT;
        }
        if (proxyPort == 0) {
            proxyPort = DEFAULT_PROXY_PORT;
        }
        HoverflyConfiguration configs = new HoverflyConfiguration(scheme, host, proxyPort, adminPort, proxyLocalHost,
                destination, proxyCaCert, authToken, adminCertificate, captureHeaders);
        HoverflyConfigValidator validator = new HoverflyConfigValidator();
        return validator.validate(configs);
    }

}
