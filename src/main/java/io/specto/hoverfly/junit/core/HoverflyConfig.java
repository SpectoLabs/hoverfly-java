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
    private static final String LOCALHOST = "http://localhost";
    private int proxyPort;
    private int adminPort;
    private boolean proxyLocalHost;
    private boolean remote = false;
    private String remoteHost;

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
     * @param proxyLocalHost whether to proxy localhost
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
     * By calling this it means a remote Hoverfly will be used - not started by Java
     *
     * @return this
     */
    public HoverflyConfig useRemoteInstance() {
        this.remote = true;
        this.remoteHost = LOCALHOST;
        return this;
    }

    /**
     * By calling this it means a remote Hoverfly will be used - not started by Java
     *
     * @param remoteHost the url of the remote hoverfly
     * @return this
     */
    public HoverflyConfig useRemoteInstance(final String remoteHost) {
        this.remote = true;
        this.remoteHost = remoteHost;
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
    public String getRemoteHost() {
        return remoteHost;
    }
}
