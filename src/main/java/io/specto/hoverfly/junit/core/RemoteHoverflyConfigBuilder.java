package io.specto.hoverfly.junit.core;

public class RemoteHoverflyConfigBuilder implements RemoteHoverflyConfig {

    private static final int DEFAULT_PROXY_PORT = 8500;
    private static final int DEFAULT_ADMIN_PORT = 8888;
    private static final int DEFAULT_HTTPS_ADMIN_PORT = 443;

    private String host;
    private String scheme;
    private String authToken;
    private int adminPort = DEFAULT_ADMIN_PORT;
    private int proxyPort = DEFAULT_PROXY_PORT;
    private String sslCert; // file name relative to test resources folder
    private String destination;
    private boolean proxyLocalHost;


    @Override
    public RemoteHoverflyConfig withHttps() {
        return this;
    }

    @Override
    public RemoteHoverflyConfig withAuthHeader() {
        return this;
    }

    @Override
    public RemoteHoverflyConfig host(String host) {
        this.host = host;
        return this;
    }

    @Override
    public RemoteHoverflyConfig withHttps(String sslCert) {
        this.sslCert = sslCert;
        this.scheme = "HTTPS";
        this.adminPort = DEFAULT_HTTPS_ADMIN_PORT;
        return this;
    }

    @Override
    public RemoteHoverflyConfig withAuthHeader(String authToken) {
        this.authToken = authToken;
        return this;
    }

    @Override
    public HoverflyConfig adminPort(int adminPort) {
        this.adminPort = adminPort;
        return this;
    }

    @Override
    public HoverflyConfig proxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
        return this;
    }

    @Override
    public HoverflyConfig destination(String destination) {
        this.destination = destination;
        return this;
    }

    @Override
    public HoverflyConfig proxyLocalHost(boolean proxyLocalHost) {
        this.proxyLocalHost = proxyLocalHost;
        return this;
    }

    @Override
    public HoverflyConfiguration build() {
        HoverflyConfiguration configs = new HoverflyConfiguration(scheme, host, proxyPort, adminPort, proxyLocalHost, destination, authToken, sslCert);
        HoverflyConfigValidator validator = new HoverflyConfigValidator();
        return validator.validate(configs);
    }
}
