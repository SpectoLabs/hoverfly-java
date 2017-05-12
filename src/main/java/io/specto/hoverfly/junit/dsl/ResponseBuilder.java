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

import io.specto.hoverfly.junit.core.model.Response;

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
    private int status = 200;

    private int delay;
    private TimeUnit delayTimeUnit;

    private ResponseBuilder() {
    }

    /**
     * Instantiates a new instance
     * @return the builder
     */
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
     * Builds a {@link Response}
     * @return the response
     */
    Response build() {
        return new Response(status, body, false, headers);
    }

    public ResponseBuilder body(final HttpBodyConverter httpBodyConverter) {
        this.body = httpBodyConverter.body();
        this.header("Content-Type", httpBodyConverter.contentType());
        return this;
    }

    /**
     * Sets delay paramters.
     * @param delay amount of delay
     * @param delayTimeUnit time unit of delay (e.g. SECONDS)
     * @return the {@link ResponseBuilder for further customizations}
     */
    public ResponseBuilder withDelay(int delay, TimeUnit delayTimeUnit) {
        this.delay = delay;
        this.delayTimeUnit = delayTimeUnit;
        return this;
    }

    ResponseDelaySettingsBuilder addDelay() {
        return new ResponseDelaySettingsBuilder(delay, delayTimeUnit);
    }

}

