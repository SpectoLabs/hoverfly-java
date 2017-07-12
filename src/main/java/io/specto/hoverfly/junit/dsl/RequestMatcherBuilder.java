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

import io.specto.hoverfly.junit.core.model.FieldMatcher;
import io.specto.hoverfly.junit.core.model.Request;
import io.specto.hoverfly.junit.core.model.RequestResponsePair;
import io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers;
import io.specto.hoverfly.junit.dsl.matchers.PlainTextFieldMatcher;
import io.specto.hoverfly.junit.dsl.matchers.RequestFieldMatcher;

import java.util.*;
import java.util.stream.Collectors;

import static io.specto.hoverfly.junit.core.model.FieldMatcher.*;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.any;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.equalsTo;

/**
 * A builder for {@link Request}
 */
public class RequestMatcherBuilder {

    private StubServiceBuilder invoker;
    private final FieldMatcher method;
    private final FieldMatcher scheme;
    private final FieldMatcher destination;
    private final FieldMatcher path;
    private final MultivaluedHashMap<PlainTextFieldMatcher, PlainTextFieldMatcher> queryPatterns = new MultivaluedHashMap<>();
    private final Map<String, List<String>> headers = new HashMap<>();
    private FieldMatcher query = blankMatcher();
    private FieldMatcher body = blankMatcher();
    private boolean isFuzzyMatchedQuery;


    RequestMatcherBuilder(final StubServiceBuilder invoker,
                          final StubServiceBuilder.HttpMethod method,
                          final FieldMatcher scheme,
                          final FieldMatcher destination,
                          final PlainTextFieldMatcher path) {
        this.invoker = invoker;
        this.method = method.getFieldMatcher();
        this.scheme = scheme;
        this.destination = destination;
        this.path = path.getFieldMatcher();
    }

    /**
     * Sets the request body
     * @param body the request body to match on exactly
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder body(final String body) {
        this.body = exactlyMatches(body);
        return this;
    }

    /**
     * Sets the request body using {@link HttpBodyConverter} to match on exactly
     * @param httpBodyConverter custom http body converter
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder body(HttpBodyConverter httpBodyConverter) {
        this.body = exactlyMatches(httpBodyConverter.body());
        return this;
    }

    public RequestMatcherBuilder body(RequestFieldMatcher matcher) {
        this.body = matcher.getFieldMatcher();
        return this;
    }

    public RequestMatcherBuilder anyBody() {
        this.body = null;
        return this;
    }

    /**
     * Sets one request header
     * @param key the header key to match on
     * @param value the header value to match on
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder header(final String key, final String value) {
        headers.put(key, Collections.singletonList(value));
        return this;
    }

    /**
     * Sets the request query
     * @param key the query params key to match on
     * @param values the query params values to match on
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder queryParam(final String key, final Object... values) {
        if (values.length == 0 ) {
            return queryParam(HoverflyMatchers.equalsTo(key), any());
        }

        for(Object value : values) {
            queryPatterns.add(equalsTo(key), equalsTo(value));
        }
        return this;
    }

    public RequestMatcherBuilder queryParam(final String key, final PlainTextFieldMatcher value) {
        return queryParam(equalsTo(key), value);
    }

    public RequestMatcherBuilder queryParam(final PlainTextFieldMatcher key, final String value) {
        return queryParam(key, equalsTo(value));
    }


    public RequestMatcherBuilder queryParam(final PlainTextFieldMatcher key, final PlainTextFieldMatcher value) {
        isFuzzyMatchedQuery = true;
        queryPatterns.add(key, value);
        return this;
    }

    public RequestMatcherBuilder anyQueryParams() {
        query = null;
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

        if (!this.queryPatterns.isEmpty()) {
            String queryPatterns = this.queryPatterns.entrySet().stream()
                    .flatMap(e -> e.getValue().stream().map(v -> e.getKey().getPattern() + "=" + v.getPattern()))
                    .collect(Collectors.joining("&"));
            query = isFuzzyMatchedQuery ? wildCardMatches(queryPatterns) : exactlyMatches(queryPatterns);
        }

        return new Request(path, method, destination, scheme, query, body, headers);
    }

    private static class MultivaluedHashMap<K, V> {
        private Map<K, List<V>> elements = new LinkedHashMap<>();

        private void add(K key, V value) {
            List<V> values;
            if (elements.containsKey(key)) {
                values = elements.get(key);
            } else {
                values = new ArrayList<>();
                elements.put(key, values);
            }
            values.add(value);
        }

        private Set<Map.Entry<K, List<V>>> entrySet() {
            return elements.entrySet();
        }

        private boolean isEmpty() {
            return elements.isEmpty();
        }

    }

}
