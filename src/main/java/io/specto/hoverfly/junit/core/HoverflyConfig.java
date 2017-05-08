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
package io.specto.hoverfly.junit.core;

/**
 * Config used to change the settings for {@link Hoverfly}
 */
public class HoverflyConfig {
    private static final String LOCALHOST = "localhost";
    private int proxyPort;
    private int adminPort;
    private boolean proxyLocalHost;
    private boolean remote;
    private String host = LOCALHOST;
    private String sslCertificatePath;
    private String sslKeyPath;
    private String destination;
    private AuthenticationConfig authenticationConfig;

    private HoverflyConfig() {
    }

    /**
     * New instance
     *
     * @return a config
     */
    public static HoverflyConfig configs() {
        return new HoverflyConfig();
    }

    /**
     * Sets the proxy port for {@link Hoverfly}
     *
     * @param proxyPort the proxy port
     * @return the {@link HoverflyConfig} for further customizations
     */
    public HoverflyConfig proxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
        return this;
    }

    /**
     * Sets the admin port for {@link Hoverfly}
     * @param adminPort the admin port
     * @return the {@link HoverflyConfig} for further customizations
     */
    public HoverflyConfig adminPort(int adminPort) {
        this.adminPort = adminPort;
        return this;
    }

    /**
     * Controls whether we want to proxy localhost.  If false then any request to localhost will not be proxied through {@link Hoverfly}.
     * @param proxyLocalHost whether to proxy localhost, default to false
     * @return the {@link HoverflyConfig} for further customizations
     */
    public HoverflyConfig proxyLocalHost(boolean proxyLocalHost) {
        this.proxyLocalHost = proxyLocalHost;
        return this;
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

    /**
     * Whether localhost should be proxied
     * @return true if proxied
     */
    public boolean isProxyLocalHost() {
        return proxyLocalHost;
    }

    /**
     * By calling this it means a remote Hoverfly will be used - not started by Java.  This method will assume the host is localhost.
     *
     * @return the {@link HoverflyConfig} for further customizations
     */
    public HoverflyConfig useRemoteInstance() {
        return useRemoteInstance(LOCALHOST, null);
    }

    /**
     * By calling this it means a remote Hoverfly will be used - not started by Java
     *
     * @param remoteHost the hostname of the remote hoverfly
     * @return the {@link HoverflyConfig} for further customizations
     */
    public HoverflyConfig useRemoteInstance(final String remoteHost) {
        return useRemoteInstance(remoteHost, null);
    }

    public HoverflyConfig useRemoteInstance(final String remoteHost, final AuthenticationConfig authenticationConfig) {
        this.remote = true;
        this.host = remoteHost;
        this.authenticationConfig = authenticationConfig;
        return this;
    }

    /**
     * True is a remote Hoverfly should be used
     *
     * @return whether it's remote or not
     */
    public boolean isRemoteInstance() {
        return remote;
    }

    /**
     * Returns the host for the remote instance of hoverfly
     *
     * @return the remote host
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the SSL certificate file for overriding default Hoverfly self-signed certificate
     * The file can be in any PEM encoded certificate, in .crt or .pem extensions
     * @param sslCertificatePath certificate file in classpath
     * @return the {@link HoverflyConfig} for further customizations
     */
    public HoverflyConfig sslCertificatePath(String sslCertificatePath) {
        this.sslCertificatePath = sslCertificatePath;
        return this;
    }


    /**
     * Sets the SSL key file for overriding default Hoverfly SSL key
     * The file can be in any PEM encoded key, in .key or .pem extensions
     * @param sslKeyPath key file in classpath
     * @return the {@link HoverflyConfig} for further customizations
     */
    public HoverflyConfig sslKeyPath(String sslKeyPath) {
        this.sslKeyPath = sslKeyPath;
        return this;
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

    public HoverflyConfig destination(String destination) {
        this.destination = destination;
        return this;
    }

    public AuthenticationConfig getAuthenticationConfig() {
        return authenticationConfig;
    }

    public static AuthenticationConfig authenticationConfigs() {
        return new AuthenticationConfig();
    }


    public static class AuthenticationConfig {
        private String proxyAuthToken;
        private String sslCert;
        private boolean isHttps;

        public AuthenticationConfig withHttps(String sslCert) {
            this.sslCert = sslCert;
            this.isHttps = true;
            return this;
        }

        public AuthenticationConfig withProxyAuthorization(String authToken) {
            this.proxyAuthToken = authToken;
            return this;
        }

        public boolean isHttps() {
            return isHttps;
        }

        public String getProxyAuthToken() {
            return proxyAuthToken;
        }

        public String getSslCert() {
            return sslCert;
        }
    }
}
