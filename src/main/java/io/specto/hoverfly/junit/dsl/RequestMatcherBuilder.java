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

import io.specto.hoverfly.junit.core.model.Request;
import io.specto.hoverfly.junit.core.model.RequestFieldMatcher;
import io.specto.hoverfly.junit.core.model.RequestResponsePair;

import java.util.*;
import java.util.stream.Collectors;

import static io.specto.hoverfly.junit.core.model.RequestFieldMatcher.newArrayMatcher;
import static io.specto.hoverfly.junit.core.model.RequestFieldMatcher.newExactMatcher;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.any;
import static java.util.Collections.singletonList;

/**
 * A builder for {@link Request}
 */
public class RequestMatcherBuilder {

    private final StubServiceBuilder invoker;
    private final List<RequestFieldMatcher<String>> method;
    private final List<RequestFieldMatcher<String>> scheme;
    private final List<RequestFieldMatcher<String>> destination;
    private final List<RequestFieldMatcher<String>> path;
    private final Map<String, List<RequestFieldMatcher>> headers = new HashMap<>();
    private final Map<String, String> requiresState = new HashMap<>();
    private final Map<String, List<RequestFieldMatcher>> query = new HashMap<>(); // default to match on empty query
    private final List<RequestFieldMatcher> body = new ArrayList<>();
    private boolean isAnyBody = false;
    private boolean isAnyQuery = false;

    RequestMatcherBuilder(final StubServiceBuilder invoker,
                          final StubServiceBuilder.HttpMethod method,
                          final List<RequestFieldMatcher<String>> scheme,
                          final List<RequestFieldMatcher<String>> destination,
                          final List<RequestFieldMatcher<String>> path) {
        this.invoker = invoker;
        this.method = method.getRequestFieldMatcher();
        this.scheme = scheme;
        this.destination = destination;
        this.path = path;
    }

    /**
     * Sets the request body
     * @param body the request body to match on exactly
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder body(final String body) {
        this.body.add(newExactMatcher(body));
        return this;
    }

    /**
     * Sets the request body using {@link HttpBodyConverter} to match on exactly
     * @param httpBodyConverter custom http body converter
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder body(HttpBodyConverter httpBodyConverter) {
        this.body.add(newExactMatcher(httpBodyConverter.body()));
        return this;
    }

    public RequestMatcherBuilder body(RequestFieldMatcher matcher) {
        this.body.add(matcher);
        return this;
    }

    public RequestMatcherBuilder anyBody() {
        this.isAnyBody = true;
        return this;
    }

    /**
     * Add a header matcher
     * @param key the header key to match on
     * @param values the header values to match on
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder header(final String key, final Object... values) {
        if (values.length == 0 ) {
            headers.put(key, singletonList(any()));
        } else {
            headers.put(key, singletonList(newArrayMatcher(Arrays.stream(values)
                    .map(Object::toString)
                    .collect(Collectors.toList()))));
        }
        return this;
    }


    /**
     * Add a header matcher
     * @param key the header key to match on
     * @param matcher the matcher for matching header values
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder header(final String key, final RequestFieldMatcher matcher) {
        headers.put(key, singletonList(matcher));
        return this;
    }

    /**
     * Sets a required state
     * @param key state key
     * @param value state value
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder withState(final String key, final String value) {
        requiresState.put(key, value);
        return this;
    }

    /**
     * Add a query matcher
     * @param key the query params key to match on
     * @param values the query params values to match on
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder queryParam(final String key, final Object... values) {
        if (values.length == 0 ) {
            query.put(key, singletonList(any()));
        } else {
            // TODO until we implement an array matcher, hoverfly currently match on array values that are joined by semicolon
            query.put(key, singletonList(newExactMatcher(Arrays.stream(values)
                    .map(Object::toString)
                    .collect(Collectors.joining(";")))));
        }
        return this;
    }

    /**
     * Add a query matcher
     * @param key the query params key to match on
     * @param matcher the matcher for matching query parameter values
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder queryParam(final String key, final RequestFieldMatcher matcher) {
        query.put(key, singletonList(matcher));
        return this;
    }

    /**
     * Add a matcher that matches any query parameters
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder anyQueryParams() {
        this.isAnyQuery = true;
        return this;
    }

    /**
     * Sets the expected response
     * @param responseBuilder the builder for response
     * @return the {@link StubServiceBuilder} for chaining the next {@link RequestMatcherBuilder}
     * @see ResponseBuilder
     */
    public StubServiceBuilder willReturn(final ResponseBuilder responseBuilder) {
        Request request = this.build();
        return invoker
                .addRequestResponsePair(new RequestResponsePair(request, responseBuilder.build()))
                .addDelaySetting(request, responseBuilder);
    }

    public Request build() {

        if (body.isEmpty()) {
            body.add(newExactMatcher("")); // default to match on empty body
        }

        Map<String, List<RequestFieldMatcher>> query = isAnyQuery ? null : this.query;
        List<RequestFieldMatcher> body = isAnyBody ? null : this.body;
        return new Request(path, method, destination, scheme, query, null, body, headers, requiresState);
    }

}
