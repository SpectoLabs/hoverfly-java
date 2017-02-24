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

import io.specto.hoverfly.junit.core.model.DelaySettings;
import io.specto.hoverfly.junit.core.model.RequestMatcher;
import io.specto.hoverfly.junit.core.model.RequestResponsePair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static io.specto.hoverfly.junit.dsl.RequestMatcherBuilder.requestMatcherBuilder;

/**
 * Used as part of the DSL for creating a {@link RequestResponsePair} used within a Hoverfly Simulation.  Each builder is locked to a single base URL.
 */
public class StubServiceBuilder {

    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final String SEPARATOR = "://";
    private static final String PATCH = "PATCH";

    private final String destination;
    private final String scheme;
    private final Set<RequestResponsePair> requestResponsePairs = new HashSet<>();
    private final List<DelaySettings> delaySettings = new ArrayList<>();

    /**
     * Instantiates builder for a given base URL
     *
     * @param baseUrl the base URL of the service you are going to simulate
     */
    StubServiceBuilder(String baseUrl) {
        //TODO null checking
        if (baseUrl.startsWith(HTTPS + SEPARATOR)) {
            this.scheme = HTTPS;
            this.destination = baseUrl.substring(HTTPS.length() + SEPARATOR.length(), baseUrl.length());
        } else if (baseUrl.startsWith(HTTP + SEPARATOR)) {
            this.scheme = HTTP;
            this.destination = baseUrl.substring(HTTP.length() + SEPARATOR.length(), baseUrl.length());
        } else {
            this.scheme = HTTP;
            this.destination = baseUrl;
        }
    }

    /**
     * Creating a GET request matcher
     *
     * @param path the path you want the matcher to have
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder get(final String path) {
        return requestMatcherBuilder(this, "GET", scheme, destination, path);
    }

    /**
     * Creating a DELETE request matcher
     *
     * @param path the path you want the matcher to have
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder delete(final String path) {
        return requestMatcherBuilder(this, "DELETE", scheme, destination, path);
    }

    /**
     * Creating a PUT request matcher
     *
     * @param path the path you want the matcher to have
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder put(final String path) {
        return requestMatcherBuilder(this, "PUT", scheme, destination, path);
    }

    /**
     * Creating a POST request matcher
     *
     * @param path the path you want the matcher to have
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder post(final String path) {
        return requestMatcherBuilder(this, "POST", scheme, destination, path);
    }


    /**
     * Creating a PATCH request matcher
     *
     * @param path the path you want the matcher to have
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder patch(final String path) {
        return requestMatcherBuilder(this, "PATCH", scheme, destination, path);
    }

    /**
     * Used for retrieving all the requestResponsePairs that the builder contains
     *
     * @return the set of {@link RequestResponsePair}
     */
    public Set<RequestResponsePair> getRequestResponsePairs() {
        return Collections.unmodifiableSet(requestResponsePairs);
    }

    /**
     * Adds a pair to this builder.  Called by the {@link RequestMatcherBuilder#willReturn} in order for the DSL to be expressive such as:
     * <p>
     * <pre>
     *
     * pairsBuilder.method("/some/path").willReturn(created()).method("/some/other/path").willReturn(noContent())
     * <pre/>
     */
    StubServiceBuilder addRequestResponsePair(final RequestResponsePair requestResponsePair) {
        this.requestResponsePairs.add(requestResponsePair);
        return this;
    }

    /**
     * Used to create url pattenrs of {@link DelaySettings}.
     *
     * @return service destination
     */
    String getDestination() {
        return this.destination;
    }

    /**
     * Adds service wide delay settings.
     *
     * @param delay         amount of delay
     * @param delayTimeUnit time unit of delay (e.g. SECONDS)
     * @return delay settings builder
     */
    public StubServiceDelaySettingsBuilder andDelay(int delay, final TimeUnit delayTimeUnit) {
        return new StubServiceDelaySettingsBuilder(delay, delayTimeUnit, this);
    }

    /**
     * Used to initialize {@link io.specto.hoverfly.junit.core.model.GlobalActions}.
     *
     * @return list of {@link DelaySettings}
     */
    public List<DelaySettings> getDelaySettings() {
        return Collections.unmodifiableList(this.delaySettings);
    }

    void addDelaySetting(final DelaySettings delaySettings) {
        if (delaySettings != null) {
            this.delaySettings.add(delaySettings);
        }
    }

    StubServiceBuilder addDelaySetting(final RequestMatcher requestMatcher, final ResponseBuilder responseBuilder) {
        responseBuilder.addDelay().to(this).forRequest(requestMatcher);
        return this;
    }
}
