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


import io.specto.hoverfly.junit.core.config.HoverflyConfiguration;
import io.specto.hoverfly.junit.core.config.LocalHoverflyConfig;
import io.specto.hoverfly.junit.core.config.RemoteHoverflyConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Config builder interface for common settings of {@link Hoverfly}
 */
public abstract class HoverflyConfig {

    protected int proxyPort;
    protected int adminPort;
    protected boolean proxyLocalHost;
    protected String destination;
    protected String proxyCaCert;
    protected List<String> captureHeaders;

    /**
     * New instance
     * @return a {@link LocalHoverflyConfig} implementation
     */
    public static LocalHoverflyConfig configs() {
        return new LocalHoverflyConfig();
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
     * Sets destination filter to what target urls to simulate or capture
     * @param destination the destination filter
     * @return the {@link HoverflyConfig} for further customizations
     */
    public HoverflyConfig destination(String destination) {
        this.destination = destination;
        return this;
    }

    /**
     * Controls whether we want to proxy localhost.  If false then any request to localhost will not be proxied through {@link Hoverfly}.
     * @return the {@link HoverflyConfig} for further customizations
     */
    @Deprecated
    public HoverflyConfig proxyLocalHost(boolean proxyLocalHost) {
        if (proxyLocalHost) {
            return proxyLocalHost();
        }
        return this;
    }

    /**
     * Invoke to enable proxying of localhost requests
     * By default it is false
     * @return a config
     */
    public HoverflyConfig proxyLocalHost() {
        this.proxyLocalHost = true;
        return this;
    }

    /**
     * Specifies which request headers to capture
     * @param headers an array of header names
     * @return the {@link HoverflyConfig} for further customizations
     */
    public HoverflyConfig captureHeaders(String... headers) {
        this.captureHeaders = Arrays.asList(headers);
        return this;
    }


    /**
     * Set to capture all request headers
     * @return the {@link HoverflyConfig} for further customizations
     */
    public HoverflyConfig captureAllHeaders() {
        this.captureHeaders = Collections.singletonList("*");
        return this;
    }


    /**
     * Set proxy CA certificate to validate the authenticity of a Hoverfly instance.
     * If your hoverfly instance is not started with custom CA cert, then this option is not required.
     * @param proxyCaCert the path for the PEM encoded certificate relative to classpath
     * @return the {@link HoverflyConfig} for further customizations
     */
    public HoverflyConfig proxyCaCert(String proxyCaCert) {
        this.proxyCaCert = proxyCaCert;
        return this;
    }


    /**
     * Enable remote Hoverfly configurations
     * @return a {@link RemoteHoverflyConfig} implementation
     */
    public RemoteHoverflyConfig remote() {
        return new RemoteHoverflyConfig();
    }

    /**
     * Validate and build {@link HoverflyConfiguration}
     * @return a validated hoverfly configuration object
     */
    public abstract HoverflyConfiguration build();
}
