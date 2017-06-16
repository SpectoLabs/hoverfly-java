package io.specto.hoverfly.junit.core.config;

import io.specto.hoverfly.junit.core.Hoverfly;

import java.util.*;

import static io.specto.hoverfly.junit.core.HoverflyConstants.HTTP;
import static io.specto.hoverfly.junit.core.HoverflyConstants.LOCALHOST;

/**
 * Configurations for Hoverfly instance
 */
public class HoverflyConfiguration {

    private String scheme = HTTP;
    private String host = LOCALHOST;
    private int proxyPort;
    private int adminPort;
    private boolean proxyLocalHost;
    private String destination;
    private String sslCertificatePath;
    private String sslKeyPath;
    private String authToken;
    private boolean isRemoteInstance;
    private String adminCertificate;
    private String proxyCaCertificate;
    private List<String> captureHeaders = Collections.emptyList();

    /**
     * Create configurations for external hoverfly
     */
    HoverflyConfiguration(final String scheme,
                          final String host,
                          final int proxyPort,
                          final int adminPort,
                          final boolean proxyLocalHost,
                          final String destination,
                          final String proxyCaCertificate,
                          final String authToken,
                          final String adminCertificate,
                          final List<String> captureHeaders) {
        this(proxyPort, adminPort, proxyLocalHost, destination, proxyCaCertificate, null, null, captureHeaders);
        setScheme(scheme);
        setHost(host);
        this.authToken = authToken;
        this.adminCertificate = adminCertificate;
        this.isRemoteInstance = true;
    }

    /**
     * Create configurations for internal-managed hoverfly
     */
    HoverflyConfiguration(final int proxyPort,
                          final int adminPort,
                          final boolean proxyLocalHost,
                          final String destination,
                          final String proxyCaCertificate,
                          final String sslCertificatePath,
                          final String sslKeyPath,
                          final List<String> captureHeaders) {
        this.proxyPort = proxyPort;
        this.adminPort = adminPort;
        this.proxyLocalHost = proxyLocalHost;
        this.destination = destination;
        this.proxyCaCertificate = proxyCaCertificate;
        this.sslCertificatePath = sslCertificatePath;
        this.sslKeyPath = sslKeyPath;
        this.captureHeaders = captureHeaders;
    }

    /**
     * Returns the host for the remote instance of hoverfly
     *
     * @return the remote host
     */
    public String getHost() {
        return host;
    }

    public String getScheme() {
        return scheme;
    }

    /**
     * Gets the proxy port {@link Hoverfly} is configured to run on
     * @return the proxy port
     */
    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * Gets the admin port {@link Hoverfly} is configured to run on
     * @return the admin port
     */
    public int getAdminPort() {
        return adminPort;
    }

    public boolean isProxyLocalHost() {
        return proxyLocalHost;
    }

    /**
     * Gets the path to SSL certificate
     * @return the SSL certificate path
     */
    public String getSslCertificatePath() {
        return sslCertificatePath;
    }

    /**
     * Gets the path to SSL key
     * @return the SSL key path
     */
    public String getSslKeyPath() {
        return sslKeyPath;
    }

    public String getDestination() {
        return destination;
    }

    public Optional<String> getAuthToken() {
        return Optional.ofNullable(authToken);
    }

    public boolean isRemoteInstance() {
        return isRemoteInstance;
    }

    public Optional<String> getProxyCaCertificate() {
        return Optional.ofNullable(proxyCaCertificate);
    }

    public String getAdminCertificate() {
        return adminCertificate;
    }

    public List<String> getCaptureHeaders() {
        return captureHeaders;
    }

    void setHost(String host) {
        if (host != null) {
            this.host = host;
        }
    }

    void setScheme(String scheme) {
        if (scheme != null) {
            this.scheme = scheme;
        }
    }

    void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    void setAdminPort(int adminPort) {
        this.adminPort = adminPort;
    }

}
