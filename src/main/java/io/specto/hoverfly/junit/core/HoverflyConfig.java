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
public interface HoverflyConfig {

    /**
     * New instance
     *
     * @return a config
     */
    static LocalHoverflyConfig configs() {
        return new HoverflyConfigBuilder();
    }

    HoverflyConfig adminPort(int port);

    HoverflyConfig proxyPort(int port);

    HoverflyConfig destination(String destination);

    @Deprecated
    default HoverflyConfig proxyLocalHost(boolean proxyLocalHost) {
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
    HoverflyConfig proxyLocalHost();


    default RemoteHoverflyConfig remote() {
        return new RemoteHoverflyConfigBuilder();
    }

    HoverflyConfiguration build();

    HoverflyConfig proxyCaCert(String proxyCaCert);
}
