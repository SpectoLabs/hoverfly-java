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
/*
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  specific language governing permissions and limitations under the License.

  Copyright 2016-2016 SpectoLabs Ltd.
 */
package io.specto.hoverfly.junit;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class HoverflyRule extends ExternalResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(HoverflyRule.class);
    private final Hoverfly hoverfly;
    private final URI simulation;
    private final HoverflyMode hoverflyMode;


    private HoverflyRule(final URI simulation, final HoverflyMode hoverflyMode, final HoverflyConfig hoverflyConfig) {
        this.hoverflyMode = hoverflyMode;
        this.hoverfly = new Hoverfly(hoverflyConfig, hoverflyMode);
        this.simulation = simulation;
    }

    /**
     * Run hoverfly in capture mode
     *
     * @param recordedFilename the path to the recorded name relative to src/test/resources
     * @return HoverflyRule
     */
    public static HoverflyRule inCaptureMode(String recordedFilename) {
        return inCaptureMode(recordedFilename, HoverflyConfig.configs());
    }

    public static HoverflyRule inCaptureMode(String recordedFilename, HoverflyConfig hoverflyConfig) {
        return new HoverflyRule(HoverflyRuleUtils.fileRelativeToTestResources(recordedFilename), HoverflyMode.CAPTURE, hoverflyConfig);
    }

    public static HoverflyRule inSimulationMode(String resourceNameOnClasspath) {
        return inSimulationMode(resourceNameOnClasspath, HoverflyConfig.configs());
    }

    public static HoverflyRule inSimulationMode(String resourceNameOnClasspath, HoverflyConfig hoverflyConfig) {
        return new HoverflyRule(HoverflyRuleUtils.findResourceOnClasspath(resourceNameOnClasspath), HoverflyMode.SIMULATE, hoverflyConfig);
    }

    public static HoverflyRule inSimulationMode(URL webResourceUrl) {
        return inSimulationMode(webResourceUrl, HoverflyConfig.configs());
    }

    public static HoverflyRule inSimulationMode(URL webResourceUrl, HoverflyConfig hoverflyConfig) {
        try {
            return new HoverflyRule(webResourceUrl.toURI(), HoverflyMode.SIMULATE, hoverflyConfig);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    protected void before() throws Throwable {

        hoverfly.start();

        if (hoverflyMode == HoverflyMode.SIMULATE) {
            hoverfly.importSimulation(simulation);
        }
    }

    @Override
    protected void after() {
        try {
            if (hoverflyMode == HoverflyMode.CAPTURE) {
                hoverfly.exportSimulation(simulation);
            }
        } catch (Exception e) {
            LOGGER.error("Error exporting", e);
        } finally {
            hoverfly.stop();
        }
    }

    public int getProxyPort() {
        return hoverfly.getProxyPort();
    }
}
