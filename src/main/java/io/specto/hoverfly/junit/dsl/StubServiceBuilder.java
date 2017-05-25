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

import io.specto.hoverfly.junit.core.model.*;
import io.specto.hoverfly.junit.dsl.matchers.ExactMatcher;
import io.specto.hoverfly.junit.dsl.matchers.PlainTextMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static io.specto.hoverfly.junit.core.model.FieldMatcher.exactlyMatches;


/**
 * Used as part of the DSL for creating a {@link RequestResponsePair} used within a Hoverfly Simulation.  Each builder is locked to a single base URL.
 */
public class StubServiceBuilder {

    private static final String SEPARATOR = "://";

    private final FieldMatcher destination;
    private FieldMatcher scheme;
    private final Set<RequestResponsePair> requestResponsePairs = new HashSet<>();
    private final List<DelaySettings> delaySettings = new ArrayList<>();

    /**
     * Instantiates builder for a given base URL
     *
     * @param baseUrl the base URL of the service you are going to simulate
     */
    StubServiceBuilder(String baseUrl) {

        String[] elements = baseUrl.split(SEPARATOR);
        if (baseUrl.contains(SEPARATOR)) {
            this.scheme = exactlyMatches(elements[0]);
            this.destination = exactlyMatches(elements[1]);
        } else {
            this.destination = exactlyMatches(elements[0]);
        }

    }

    StubServiceBuilder(PlainTextMatcher matcher) {
        this.destination = matcher.getFieldMatcher();
    }


    /**
     * Creating a GET request matcher
     *
     * @param path the path you want the matcher to have
     * @return the {@link RequestTemplateBuilder} for further customizations
     */
    public RequestTemplateBuilder get(final String path) {
        return get(ExactMatcher.newInstance(path));
    }

    public RequestTemplateBuilder get(final PlainTextMatcher path) {
        return new RequestTemplateBuilder(this, exactlyMatches("GET"), scheme, destination, path.getFieldMatcher());
    }

    /**
     * Creating a DELETE request matcher
     *
     * @param path the path you want the matcher to have
     * @return the {@link RequestTemplateBuilder} for further customizations
     */
    public RequestTemplateBuilder delete(final String path) {
        return delete(ExactMatcher.newInstance(path));
    }

    public RequestTemplateBuilder delete(PlainTextMatcher path) {
        return new RequestTemplateBuilder(this, exactlyMatches("DELETE"), scheme, destination, path.getFieldMatcher());
    }

    /**
     * Creating a PUT request matcher
     *
     * @param path the path you want the matcher to have
     * @return the {@link RequestTemplateBuilder} for further customizations
     */
    public RequestTemplateBuilder put(final String path) {
        return put(ExactMatcher.newInstance(path));
    }


    public RequestTemplateBuilder put(PlainTextMatcher path) {
        return new RequestTemplateBuilder(this, exactlyMatches("PUT"), scheme, destination, path.getFieldMatcher());
    }

    /**
     * Creating a POST request matcher
     *
     * @param path the path you want the matcher to have
     * @return the {@link RequestTemplateBuilder} for further customizations
     */
    public RequestTemplateBuilder post(final String path) {
        return post(ExactMatcher.newInstance(path));
    }

    public RequestTemplateBuilder post(PlainTextMatcher path) {
        return new RequestTemplateBuilder(this, exactlyMatches("POST"), scheme, destination, path.getFieldMatcher());
    }

    /**
     * Creating a PATCH request matcher
     *
     * @param path the path you want the matcher to have
     * @return the {@link RequestTemplateBuilder} for further customizations
     */
    public RequestTemplateBuilder patch(final String path) {
        return patch(ExactMatcher.newInstance(path));
    }

    public RequestTemplateBuilder patch(PlainTextMatcher path) {
        return new RequestTemplateBuilder(this, exactlyMatches("PATCH"), scheme, destination, path.getFieldMatcher());
    }

    public RequestTemplateBuilder anyMethod(String path) {
        return anyMethod(ExactMatcher.newInstance(path));
    }

    public RequestTemplateBuilder anyMethod(PlainTextMatcher path) {
        return new RequestTemplateBuilder(this, null, scheme, destination, path.getFieldMatcher());
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
     * Adds a pair to this builder.  Called by the {@link RequestTemplateBuilder#willReturn} in order for the DSL to be expressive such as:
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
    // TODO it needs to support glob pattern
    String getDestination() {
        return this.destination.getExactMatch();
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
     * Used to initialize {@link GlobalActions}.
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

    StubServiceBuilder addDelaySetting(final Request request, final ResponseBuilder responseBuilder) {
        responseBuilder.addDelay().to(this).forRequest(request);
        return this;
    }
}
