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
public class HoverflyConfigBuilder implements LocalHoverflyConfig {

    private int proxyPort;
    private int adminPort;
    private boolean proxyLocalHost;
    // TODO should be combined field?
    private String sslCertificatePath;
    private String sslKeyPath;
    private String destination;
    private String proxyCaCert;


    /**
     * Sets the proxy port for {@link Hoverfly}
     *
     * @param proxyPort the proxy port
     * @return the {@link HoverflyConfigBuilder} for further customizations
     */
    @Override
    public HoverflyConfig proxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
        return this;
    }


    /**
     * Sets the admin port for {@link Hoverfly}
     * @param adminPort the admin port
     * @return the {@link HoverflyConfigBuilder} for further customizations
     */
    @Override
    public HoverflyConfig adminPort(int adminPort) {
        this.adminPort = adminPort;
        return this;
    }

    /**
     * Controls whether we want to proxy localhost.  If false then any request to localhost will not be proxied through {@link Hoverfly}.
     * @return the {@link HoverflyConfigBuilder} for further customizations
     */
    @Override
    public HoverflyConfig proxyLocalHost() {
        this.proxyLocalHost = true;
        return this;
    }

    /**
     * Sets the SSL certificate file for overriding default Hoverfly self-signed certificate
     * The file can be in any PEM encoded certificate, in .crt or .pem extensions
     * @param sslCertificatePath certificate file in classpath
     * @return the {@link HoverflyConfigBuilder} for further customizations
     */
    @Override
    public LocalHoverflyConfig sslCertificatePath(String sslCertificatePath) {
        this.sslCertificatePath = sslCertificatePath;
        return this;
    }


    /**
     * Sets the SSL key file for overriding default Hoverfly SSL key
     * The file can be in any PEM encoded key, in .key or .pem extensions
     * @param sslKeyPath key file in classpath
     * @return the {@link HoverflyConfigBuilder} for further customizations
     */
    @Override
    public LocalHoverflyConfig sslKeyPath(String sslKeyPath) {
        this.sslKeyPath = sslKeyPath;
        return this;
    }


    @Override
    public HoverflyConfig proxyCaCert(String proxyCaCert) {
        this.proxyCaCert = proxyCaCert;
        return this;
    }

    @Override
    public HoverflyConfig destination(String destination) {
        this.destination = destination;
        return this;
    }

    @Override
    public HoverflyConfiguration build() {
        HoverflyConfiguration configs = new HoverflyConfiguration(proxyPort, adminPort, proxyLocalHost, destination, proxyCaCert, sslCertificatePath, sslKeyPath);
        HoverflyConfigValidator validator = new HoverflyConfigValidator();
        return validator.validate(configs);
    }


}
