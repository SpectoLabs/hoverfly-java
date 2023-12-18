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

import java.util.*;
import java.util.concurrent.TimeUnit;

import static io.specto.hoverfly.junit.core.model.RequestFieldMatcher.*;
import static io.specto.hoverfly.junit.dsl.StubServiceBuilder.HttpMethod.*;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.equalsTo;
import static java.util.Collections.singletonList;


/**
 * Used as part of the DSL for creating a {@link RequestResponsePair} used within a Hoverfly Simulation.  Each builder is locked to a single base URL.
 */
public class StubServiceBuilder {

    private final Set<RequestResponsePair> requestResponsePairs = new LinkedHashSet<>();
    private final List<DelaySettings> delaySettings = new ArrayList<>();

    private static final String SEPARATOR = "://";
    protected final List<RequestFieldMatcher<String>> destination;
    protected List<RequestFieldMatcher<String>> scheme;


    /**
     * Creating a GET request matcher
     *
     * @param path the path you want the matcher to have
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder get(final String path) {
        return get(equalsTo(path));
    }


    public RequestMatcherBuilder get(final RequestFieldMatcher path) {
        return createRequestMatcherBuilder(GET, path);
    }

    /**
     * Creating a DELETE request matcher
     *
     * @param path the path you want the matcher to have
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder delete(final String path) {
        return delete(equalsTo(path));
    }

    public RequestMatcherBuilder delete(RequestFieldMatcher path) {
        return createRequestMatcherBuilder(DELETE, path);
    }

    /**
     * Creating a PUT request matcher
     *
     * @param path the path you want the matcher to have
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder put(final String path) {
        return put(equalsTo(path));
    }


    public RequestMatcherBuilder put(RequestFieldMatcher path) {
        return createRequestMatcherBuilder(PUT, path);
    }

    /**
     * Creating a POST request matcher
     *
     * @param path the path you want the matcher to have
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder post(final String path) {
        return post(equalsTo(path));
    }

    public RequestMatcherBuilder post(RequestFieldMatcher path) {
        return createRequestMatcherBuilder(POST, path);
    }

    /**
     * Creating a PATCH request matcher
     *
     * @param path the path you want the matcher to have
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder patch(final String path) {
        return patch(equalsTo(path));
    }

    public RequestMatcherBuilder patch(RequestFieldMatcher path) {
        return createRequestMatcherBuilder(PATCH, path);
    }

    /**
     * Creating a OPTIONS request matcher
     *
     * @param path the path you want the matcher to have
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder options(final String path) {
        return options(equalsTo(path));
    }

    public RequestMatcherBuilder options(RequestFieldMatcher path) {
        return createRequestMatcherBuilder(OPTIONS, path);
    }

    /**
     * Creating a HEAD request matcher
     *
     * @param path the path you want the matcher to have
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder head(final String path) {
        return head(equalsTo(path));
    }

    public RequestMatcherBuilder head(RequestFieldMatcher path) {
        return createRequestMatcherBuilder(HEAD, path);
    }

    /**
     * Creating a CONNECT request matcher
     *
     * @param path the path you want the matcher to have
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder connect(final String path) {
        return connect(equalsTo(path));
    }

    public RequestMatcherBuilder connect(RequestFieldMatcher path) {
        return createRequestMatcherBuilder(CONNECT, path);
    }

    public RequestMatcherBuilder anyMethod(String path) {
        return anyMethod(equalsTo(path));
    }

    public RequestMatcherBuilder anyMethod(RequestFieldMatcher path) {
        return createRequestMatcherBuilder(ANY, path);
    }


    /**
     * Instantiates builder for a given base URL
     *
     * @param baseUrl the base URL of the service you are going to simulate
     */
    StubServiceBuilder(String baseUrl) {
        String[] elements = baseUrl.split(SEPARATOR);
        if (baseUrl.contains(SEPARATOR)) {
            this.scheme = singletonList(newExactMatcher(elements[0]));
            this.destination = singletonList(newExactMatcher(elements[1]));
        } else {
            this.destination = singletonList(newExactMatcher(elements[0]));
        }
    }

    StubServiceBuilder(RequestFieldMatcher matcher) {
        this.destination = singletonList(matcher);
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

    private RequestMatcherBuilder createRequestMatcherBuilder(HttpMethod httpMethod, RequestFieldMatcher<String> path) {
        return new RequestMatcherBuilder(this, httpMethod, scheme, destination, singletonList(path));
    }

    enum HttpMethod {
        GET,
        PUT,
        POST,
        DELETE,
        PATCH,
        OPTIONS,
        CONNECT,
        HEAD,
        ANY;

        List<RequestFieldMatcher<String>> getRequestFieldMatcher() {
            List<RequestFieldMatcher<String>> matchers = null;
            if (this != ANY) {
                matchers = singletonList(newExactMatcher(this.name()));
            }
            return matchers;
        }
    }
}
