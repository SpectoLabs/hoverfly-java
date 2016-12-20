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
package io.specto.hoverfly.junit.rule;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyConfig;
import io.specto.hoverfly.junit.core.HoverflyMode;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static io.specto.hoverfly.junit.core.HoverflyMode.CAPTURE;
import static io.specto.hoverfly.junit.core.HoverflyMode.SIMULATE;
import static io.specto.hoverfly.junit.rule.HoverflyRuleUtils.fileRelativeToTestResources;
import static io.specto.hoverfly.junit.rule.HoverflyRuleUtils.findResourceOnClasspath;
import static io.specto.hoverfly.junit.rule.HoverflyRuleUtils.isAnnotatedWithRule;

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
        return new HoverflyRule(fileRelativeToTestResources(recordedFilename), CAPTURE, hoverflyConfig);
    }

    public static HoverflyRule inSimulationMode(String resourceNameOnClasspath) {
        return inSimulationMode(resourceNameOnClasspath, HoverflyConfig.configs());
    }

    public static HoverflyRule inSimulationMode(String resourceNameOnClasspath, HoverflyConfig hoverflyConfig) {
        return new HoverflyRule(findResourceOnClasspath(resourceNameOnClasspath), SIMULATE, hoverflyConfig);
    }

    public static HoverflyRule inSimulationMode(URL webResourceUrl) {
        return inSimulationMode(webResourceUrl, HoverflyConfig.configs());
    }

    public static HoverflyRule inSimulationMode(URL webResourceUrl, HoverflyConfig hoverflyConfig) {
        try {
            return new HoverflyRule(webResourceUrl.toURI(), SIMULATE, hoverflyConfig);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Statement apply(Statement base, Description description) {
        if (isAnnotatedWithRule(description)) {
            LOGGER.warn("It is recommended to use HoverflyRule with @ClassRule to get better performance in your tests, and prevent known issue with Apache HttpClient. For more information, please see https://github.com/SpectoLabs/hoverfly-java.");
        }
        return super.apply(base, description);
    }

    @Override
    protected void before() throws Throwable {
        hoverfly.start();

        if (hoverflyMode == SIMULATE) {
            hoverfly.importSimulation(simulation);
        }
    }

    @Override
    protected void after() {
        try {
            if (hoverflyMode == CAPTURE) {
                hoverfly.exportSimulation(simulation);
            }
        } finally {
            hoverfly.stop();
        }
    }

    public int getProxyPort() {
        return hoverfly.getProxyPort();
    }
}
