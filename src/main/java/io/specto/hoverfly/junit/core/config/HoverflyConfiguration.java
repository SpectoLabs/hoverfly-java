package io.specto.hoverfly.junit.core.config;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.SimulationPreprocessor;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

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
    private LocalMiddleware localMiddleware;
    private List<String> captureHeaders;
    private boolean webServer;
    private boolean tlsVerificationDisabled;
    private boolean plainHttpTunneling;
    private String upstreamProxy;
    private Logger hoverflyLogger;
    private boolean statefulCapture;
    private SimulationPreprocessor simulationPreprocessor;
    private String binaryNameFormat;
    private List<String> commands;

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
                          final List<String> captureHeaders,
                          final boolean webServer,
                          final boolean statefulCapture,
                          final SimulationPreprocessor preprocessor) {
        this(proxyPort, adminPort, proxyLocalHost, destination, proxyCaCertificate, captureHeaders, webServer, null, statefulCapture, preprocessor);
        setScheme(scheme);
        setHost(host);
        this.authToken = authToken;
        this.adminCertificate = adminCertificate;
        this.isRemoteInstance = true;
    }

    /**
     * Create configurations for internal-managed hoverfly
     */
    public HoverflyConfiguration(final int proxyPort,
                          final int adminPort,
                          final boolean proxyLocalHost,
                          final String destination,
                          final String proxyCaCertificate,
                          final List<String> captureHeaders,
                          final boolean webServer,
                          final Logger hoverflyLogger,
                          final boolean statefulCapture,
                          final SimulationPreprocessor preprocessor
    ) {
        this.proxyPort = proxyPort;
        this.adminPort = adminPort;
        this.proxyLocalHost = proxyLocalHost;
        this.destination = destination;
        this.proxyCaCertificate = proxyCaCertificate;
        this.captureHeaders = captureHeaders;
        this.webServer = webServer;
        this.hoverflyLogger = hoverflyLogger;
        this.statefulCapture = statefulCapture;
        this.simulationPreprocessor = preprocessor;
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

    public void setSslCertificatePath(String sslCertificatePath) {
        this.sslCertificatePath = sslCertificatePath;
    }

    public void setSslKeyPath(String sslKeyPath) {
        this.sslKeyPath = sslKeyPath;
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

    public String getUpstreamProxy() {
        return upstreamProxy;
    }


    public void setUpstreamProxy(String upstreamProxy) {
        this.upstreamProxy = upstreamProxy;
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

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public  void setAdminPort(int adminPort) {
        this.adminPort = adminPort;
    }

    public boolean isWebServer() {
        return webServer;
    }

    public void setWebServer(boolean webServer) {
        this.webServer = webServer;
    }

    public boolean isTlsVerificationDisabled() {
        return tlsVerificationDisabled;
    }

    public void setTlsVerificationDisabled(boolean tlsVerificationDisabled) {
        this.tlsVerificationDisabled = tlsVerificationDisabled;
    }

    public boolean isPlainHttpTunneling() {
        return plainHttpTunneling;
    }

    public void setPlainHttpTunneling(boolean plainHttpTunneling) {
        this.plainHttpTunneling = plainHttpTunneling;
    }

    public LocalMiddleware getLocalMiddleware() {
        return localMiddleware;
    }

    public void setLocalMiddleware(LocalMiddleware localMiddleware) {
        this.localMiddleware = localMiddleware;
    }

    public boolean isMiddlewareEnabled() {
        return localMiddleware != null && isNotBlank(localMiddleware.getBinary()) && isNotBlank(localMiddleware.getPath());
    }

    public Optional<Logger> getHoverflyLogger() {
        return Optional.ofNullable(hoverflyLogger);
    }

    public boolean isStatefulCapture() {
        return statefulCapture;
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public void setSimulationPreprocessor(SimulationPreprocessor simulationPreprocessor) {
        this.simulationPreprocessor = simulationPreprocessor;
    }

    public Optional<SimulationPreprocessor> getSimulationPreprocessor() {
        return Optional.ofNullable(simulationPreprocessor);
    }

    public String getBinaryNameFormat() {
        return binaryNameFormat;
    }

    public void setBinaryNameFormat(String binaryNameFormat) {
        this.binaryNameFormat = binaryNameFormat;
    }


    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }
}
