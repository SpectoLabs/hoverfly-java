/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this classpath except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2016-2016 SpectoLabs Ltd.
 */
package io.specto.hoverfly.junit.core.config;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Config builder interface for settings specific to {@link Hoverfly} managed internally
 */
public class LocalHoverflyConfig extends HoverflyConfig {

    private String caCertPath;
    private String caKeyPath;
    private boolean tlsVerificationDisabled;
    private boolean plainHttpTunneling;
    private LocalMiddleware localMiddleware;
    private String upstreamProxy;
    private Logger hoverflyLogger = LoggerFactory.getLogger("hoverfly");
    private LogLevel logLevel;
    private List<String> commands = new LinkedList<>();
    private String binaryLocation;
    private String clientCertPath;
    private String clientKeyPath;
    private String clientAuthDestination;
    private String clientCaCertPath;
    private Duration healthCheckTimeout;
    private Duration healthCheckRetryInterval;

    /**
     * Sets the certificate file to override the default Hoverfly's CA cert
     * The file can be in any PEM encoded certificate, in .crt or .pem extensions
     * @param sslCertificatePath certificate file in classpath
     * @return the {@link LocalHoverflyConfig} for further customizations
     */
    @Deprecated
    public LocalHoverflyConfig sslCertificatePath(String sslCertificatePath) {
        this.caCertPath = sslCertificatePath;
        return this;
    }

    /**
     * Sets the key file for Hoverfly's CA cert
     * The file can be in any PEM encoded key, in .key or .pem extensions
     * @param sslKeyPath key file in classpath
     * @return the {@link LocalHoverflyConfig} for further customizations
     */
    @Deprecated
    public LocalHoverflyConfig sslKeyPath(String sslKeyPath) {
        this.caKeyPath = sslKeyPath;
        return this;
    }

    /**
     * Sets the certificate and key files to override the default Hoverfly's CA cert
     * @param certPath certificate file in classpath. Must be a PEM encoded certificate, with .crt or .pem extensions
     * @param keyPath key file in classpath. Must be any PEM encoded key, with .key or .pem extensions
     * @return the {@link LocalHoverflyConfig} for further customizations
     */
    public LocalHoverflyConfig overrideDefaultCaCert(String certPath, String keyPath) {
        this.caCertPath = certPath;
        this.caKeyPath = keyPath;
        return this;
    }

    /**
     * Sets the middleware for Hoverfly
     * @param binary absolute or relative path of binary
     * @param path middleware script file in classpath
     * @return the {@link LocalHoverflyConfig} for further customizations
     */
    public LocalHoverflyConfig localMiddleware(String binary, String path) {
        this.localMiddleware = new LocalMiddleware(binary, path) ;
        return this;
    }

    /**
     * Configure Hoverfly to skip TLS verification. This option allows Hoverfly to perform “insecure” SSL connections to target server that uses invalid certificate (eg. self-signed certificate)
     * @return the {@link LocalHoverflyConfig} for further customizations
     */
    public LocalHoverflyConfig disableTlsVerification() {
        this.tlsVerificationDisabled = true;
        return this;
    }

    /**
     * Invoke to enable plain http tunneling
     * By default it is false
     * @return a config
     */
    public LocalHoverflyConfig plainHttpTunneling() {
        this.plainHttpTunneling = true;
        return this;
    }

    /**
     * Set upstream proxy for hoverfly to connect to target host
     * @param proxyAddress socket address of the upstream proxy, eg. 127.0.0.1:8500
     * @return the {@link LocalHoverflyConfig} for further customizations
     */
    public LocalHoverflyConfig upstreamProxy(InetSocketAddress proxyAddress) {
        this.upstreamProxy = proxyAddress.getHostString() + ":" + proxyAddress.getPort();
        return this;
    }

    public LocalHoverflyConfig upstreamProxy(String upstreamProxy) {
        this.upstreamProxy = upstreamProxy;
        return this;
    }

    /**
     * Set the name of the logger to use when logging the output of the Hoverfly binary.
     * @param loggerName Name of the logger to use when logging the output of the Hoverfly binary.
     * @return the {@link LocalHoverflyConfig} for further customizations
     */
    public LocalHoverflyConfig logger(final String loggerName) {
        this.hoverflyLogger = LoggerFactory.getLogger(loggerName);
        return this;
    }

    /**
     * Change the Hoverfly binary to output directly to {@link System#out}.
     * @return the {@link LocalHoverflyConfig} for further customizations
     */
    public LocalHoverflyConfig logToStdOut() {
        this.hoverflyLogger = null;
        return this;
    }

    /**
     * Set the log level of Hoverfly. The default level is INFO.
     * @param logLevel {@link LogLevel} to set
     * @return the {@link LocalHoverflyConfig} for further customizations
     */
    public LocalHoverflyConfig logLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    /**
     * Set additional commands for starting Hoverfly.
     * @param commands More Hoverfly command flags.
     * @return the {@link LocalHoverflyConfig} for further customizations
     */
    public LocalHoverflyConfig addCommands(String... commands) {

        this.commands.addAll(Arrays.asList(commands));
        return this;
    }

    /**
     * Overrides the default path for Hoverfly binary and working directory.
     * @param binaryLocation absolute path for the Hoverfly working directory
     * @return the {@link LocalHoverflyConfig} for further customizations
     */
    public HoverflyConfig binaryLocation(String binaryLocation) {
        this.binaryLocation = binaryLocation;
        return this;
    }

    /**
     * Set client certificate and key for mutual TLS authentication with target server
     * @param clientCertPath certificate file in classpath. Must be a PEM encoded certificate, with .crt or .pem extensions
     * @param clientKeyPath key file in classpath. Must be unencrypted and PEM encoded key, with .key or .pem extensions
     * @param destinations the destination filter to what target urls to enable mutual TLS authentication. Enable for all remote hosts if not provided.
     * @return the {@link LocalHoverflyConfig} for further customizations
     */
    public LocalHoverflyConfig enableClientAuth(String clientCertPath, String clientKeyPath, String... destinations) {
        this.clientCertPath = clientCertPath;
        this.clientKeyPath = clientKeyPath;
        if (destinations != null) {
            if (destinations.length == 0) {
                this.clientAuthDestination = ".";
            } else {
                this.clientAuthDestination = String.join("|", destinations);
            }
        }
        return this;
    }

    /**
     * Set client CA certificate for mutual TLS authentication
     * @param clientCaCertPath CA certificate file in classpath. Must be any PEM encoded certificate, with .crt or .pem extensions
     * @return the {@link LocalHoverflyConfig} for further customizations
     */
    public LocalHoverflyConfig clientAuthCaCertPath(String clientCaCertPath) {
        this.clientCaCertPath = clientCaCertPath;
        return this;
    }

    /**
     * Set the maximum time to wait for Hoverfly to be healthy.
     * @param healthCheckTimeout the health check timeout
     * @return the {@link HoverflyConfig} for further customizations
     */
    public HoverflyConfig healthCheckTimeout(Duration healthCheckTimeout) {
        this.healthCheckTimeout = healthCheckTimeout;
        return this;
    }

    /**
     * Set the interval between health checks.
     * @param healthCheckRetryInterval the health check retry interval
     * @return the {@link HoverflyConfig} for further customizations
     */
    public HoverflyConfig healthCheckRetryInterval(Duration healthCheckRetryInterval) {
        this.healthCheckRetryInterval = healthCheckRetryInterval;
        return this;
    }

    @Override
    public HoverflyConfiguration build() {
        HoverflyConfiguration configs = new HoverflyConfiguration(proxyPort, adminPort, proxyLocalHost, destination,
                proxyCaCert, captureHeaders, webServer, hoverflyLogger, logLevel, statefulCapture, incrementalCapture, simulationPreprocessor);
        configs.setSslCertificatePath(caCertPath);
        configs.setSslKeyPath(caKeyPath);
        configs.setTlsVerificationDisabled(tlsVerificationDisabled);
        configs.setPlainHttpTunneling(plainHttpTunneling);
        configs.setLocalMiddleware(localMiddleware);
        configs.setUpstreamProxy(upstreamProxy);
        configs.setCommands(commands);
        configs.setBinaryLocation(binaryLocation);
        configs.setClientCertPath(clientCertPath);
        configs.setClientKeyPath(clientKeyPath);
        configs.setClientAuthDestination(clientAuthDestination);
        configs.setClientCaCertPath(clientCaCertPath);
        configs.setResponseBodyFilesPath(responseBodyFilesPath);
        configs.setRelativeResponseBodyFilesPath(isRelativeResponseBodyFilesPath);
        configs.setHealthCheckTimeout(healthCheckTimeout);
        configs.setHealthCheckRetryInterval(healthCheckRetryInterval);
        HoverflyConfigValidator validator = new HoverflyConfigValidator();
        return validator.validate(configs);
    }
}
