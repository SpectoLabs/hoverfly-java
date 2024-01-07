/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * <p>
 * Copyright 2016-2016 SpectoLabs Ltd.
 */
package io.specto.hoverfly.junit.dsl;

import io.specto.hoverfly.junit.core.model.LogNormalDelay;
import io.specto.hoverfly.junit.core.model.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;

/**
 * A builder for building {@link Response}
 *
 * @see ResponseCreators
 */
public class ResponseBuilder {

    private final Map<String, List<String>> headers = new HashMap<>();
    private String body = "";
    private boolean encodedBody = false;
    private int status = 200;
    private boolean templated = true;
    private final Map<String, String> transitionsState = new HashMap<>();
    private final List<String> removesState = new ArrayList<>();

    private int fixedDelay;
    private LogNormalDelay logNormalDelay;

    // Deprecated: For global delay settings
    private int delay;
    private TimeUnit delayTimeUnit;

    ResponseBuilder() {
    }

    /**
     * Instantiates a new instance
     * @return the builder
     */
    @Deprecated
    public static ResponseBuilder response() {
        return new ResponseBuilder();
    }

    /**
     * Sets the body
     * @param body body of the response
     * @return the {@link ResponseBuilder for further customizations}
     */
    public ResponseBuilder body(final String body) {
        this.body = body;
        return this;
    }

    /**
     * Sets the status
     * @param status status of the response
     * @return the {@link ResponseBuilder for further customizations}
     */
    public ResponseBuilder status(final int status) {
        this.status = status;
        return this;
    }

    /**
     * Sets a header
     * @param key header name
     * @param value header value
     * @return the {@link ResponseBuilder for further customizations}
     */
    public ResponseBuilder header(final String key, final String value) {
        this.headers.put(key, singletonList(value));
        return this;
    }

    /**
     * Sets a transition state
     * @param key state key
     * @param value state value
     * @return the {@link ResponseBuilder for further customizations}
     */
    public ResponseBuilder andSetState(final String key, final String value) {
        this.transitionsState.put(key, value);
        return this;
    }

    /**
     * Sets state to be removed
     * @param stateToRemove a state to be removed
     * @return the {@link ResponseBuilder for further customizations}
     */
    public ResponseBuilder andRemoveState(final String stateToRemove) {
        this.removesState.add(stateToRemove);
        return this;
    }

    /**
     * Builds a {@link Response}
     * @return the response
     */
    Response build() {
        return new Response(status, body, encodedBody, templated, headers, transitionsState, removesState, fixedDelay, logNormalDelay, null);
    }

    public ResponseBuilder body(final HttpBodyConverter httpBodyConverter) {
        this.body = httpBodyConverter.body();
        this.header("Content-Type", httpBodyConverter.contentType());
        return this;
    }


    public ResponseBuilder disableTemplating() {
        this.templated = false;
        return this;
    }

    /**
     * Sets the body as an encodedBody for binary responses
     * @return the {@link ResponseBuilder for further customizations}
     */
    public ResponseBuilder binaryEncoding() {
        this.encodedBody = true;
        return this;
    }

    /**
     * Set fixed delay for the request-response pair
     * @param delay amount of delay
     * @param delayTimeUnit time unit of delay (e.g. SECONDS)
     * @return the {@link ResponseBuilder for further customizations}
     */
    public ResponseBuilder withFixedDelay(int delay, TimeUnit delayTimeUnit) {
        fixedDelay = (int) delayTimeUnit.toMillis(delay);
        return this;
    }

    /**
     * Set Log Normal delay for the request-response pair. The delay value will be generated randomly based on the distribution
     * which is defined by 2 parameters μ and σ. We will compute these parameters from the mean and median of a server response time.
     * You can typically find these values in your monitoring of the production server.
     * @param mean mean value of the delays to simulate
     * @param median median value of the delays to simulate
     * @param delayTimeUnit time unit of delay (e.g. SECONDS)
     * @return the {@link ResponseBuilder for further customizations}
     */
    public ResponseBuilder withLogNormalDelay(int mean, int median, TimeUnit delayTimeUnit) {
        logNormalDelay = new LogNormalDelay(
                0,
                0,
                (int) delayTimeUnit.toMillis(mean),
                (int) delayTimeUnit.toMillis(median));
        return this;
    }

    /**
     * Set Log Normal delay for the request-response pair. The delay value will be generated randomly based on the distribution
     * which is defined by 2 parameters μ and σ. We will compute these parameters from the mean and median of a server response time.
     * You can typically find these values in your monitoring of the production server.
     * @param mean mean value of the delays to simulate
     * @param median median value of the delays to simulate
     * @param min lower bound of the delays
     * @param max upper bound of the delays
     * @param delayTimeUnit time unit of delay (e.g. SECONDS)
     * @return the {@link ResponseBuilder for further customizations}
     */
    public ResponseBuilder withLogNormalDelay(int mean, int median, int min, int max, TimeUnit delayTimeUnit) {
        logNormalDelay = new LogNormalDelay(
                (int) delayTimeUnit.toMillis(min),
                (int) delayTimeUnit.toMillis(max),
                (int) delayTimeUnit.toMillis(mean),
                (int) delayTimeUnit.toMillis(median));
        return this;
    }

    /**
     * Sets delay parameters.
     * @param delay amount of delay
     * @param delayTimeUnit time unit of delay (e.g. SECONDS)
     * @return the {@link ResponseBuilder for further customizations}
     */
    @Deprecated
    public ResponseBuilder withDelay(int delay, TimeUnit delayTimeUnit) {
        this.delay = delay;
        this.delayTimeUnit = delayTimeUnit;
        return this;
    }

    ResponseDelaySettingsBuilder addDelay() {
        return new ResponseDelaySettingsBuilder(delay, delayTimeUnit);
    }
}

