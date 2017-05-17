package io.specto.hoverfly.junit.core;

class RemoteHoverflyConfigBuilder implements RemoteHoverflyConfig {

    private String host;
    private String scheme;
    private String authToken;
    private int adminPort = HoverflyConstants.DEFAULT_ADMIN_PORT;
    private int proxyPort = HoverflyConstants.DEFAULT_PROXY_PORT;
    private String adminCertificate; // file name relative to test resources folder
    private String proxyCaCertificate;
    private String destination;
    private boolean proxyLocalHost;


    @Override
    public RemoteHoverflyConfig withAuthHeader() {
        this.authToken = System.getenv(HoverflyConstants.HOVERFLY_AUTH_TOKEN);
        return this;
    }

    @Override
    public RemoteHoverflyConfig host(String host) {
        this.host = host;
        return this;
    }

    @Override
    public RemoteHoverflyConfig withAuthHeader(String authToken) {
        this.authToken = authToken;
        return this;
    }

    @Override
    public RemoteHoverflyConfig withHttpsAdminEndpoint() {
        this.scheme = HoverflyConstants.HTTPS;
        this.adminPort = HoverflyConstants.DEFAULT_HTTPS_ADMIN_PORT;
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
    public HoverflyConfig proxyLocalHost() {
        this.proxyLocalHost = true;
        return this;
    }

    @Override
    public HoverflyConfig proxyCaCert(String proxyCaCert) {
        this.proxyCaCertificate = proxyCaCert;
        return this;
    }

    @Override
    public HoverflyConfiguration build() {
        HoverflyConfiguration configs = new HoverflyConfiguration(scheme, host, proxyPort, adminPort, proxyLocalHost, destination, proxyCaCertificate, authToken, adminCertificate);
        HoverflyConfigValidator validator = new HoverflyConfigValidator();
        return validator.validate(configs);
    }

}
