/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
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

public class HoverflyConfig {
    private int proxyPort;
    private int adminPort;
    private boolean proxyLocalHost;

    private HoverflyConfig() {
    }

    public static HoverflyConfig configs() {
        return new HoverflyConfig();
    }


    public HoverflyConfig proxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
        return this;
    }

    public HoverflyConfig adminPort(int adminPort) {
        this.adminPort = adminPort;
        return this;
    }

    public HoverflyConfig proxyLocalHost(boolean proxyLocalHost) {
        this.proxyLocalHost = proxyLocalHost;
        return this;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public int getAdminPort() {
        return adminPort;
    }

    public boolean isProxyLocalHost() {
        return proxyLocalHost;
    }
}
