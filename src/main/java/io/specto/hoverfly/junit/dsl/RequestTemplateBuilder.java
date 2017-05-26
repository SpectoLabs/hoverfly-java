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
import io.specto.hoverfly.junit.dsl.matchers.ExactMatcher;
import io.specto.hoverfly.junit.dsl.matchers.PlainTextMatcher;
import io.specto.hoverfly.junit.dsl.matchers.RequestMatcher;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static io.specto.hoverfly.junit.core.model.FieldMatcher.blankMatcher;
import static io.specto.hoverfly.junit.core.model.FieldMatcher.exactlyMatches;
import static io.specto.hoverfly.junit.core.model.FieldMatcher.wildCardMatches;
import static org.apache.commons.lang3.CharEncoding.UTF_8;

/**
 * A builder for {@link Request}
 */
public class RequestTemplateBuilder {

    private final StubServiceBuilder invoker;
    private final FieldMatcher method;
    private final FieldMatcher scheme;
    private final FieldMatcher destination;
    private final FieldMatcher path;
    private final MultivaluedHashMap<PlainTextMatcher, PlainTextMatcher> queryPatterns = new MultivaluedHashMap<>();
    private final Map<String, List<String>> headers = new HashMap<>();
    private FieldMatcher query = blankMatcher();
    private FieldMatcher body = blankMatcher();
    private boolean isFuzzyMatchedQuery;

    RequestTemplateBuilder(final StubServiceBuilder invoker, final FieldMatcher method, final FieldMatcher scheme, final FieldMatcher destination, final FieldMatcher path) {
        this.invoker = invoker;
        this.method = method;
        this.scheme = scheme;
        this.destination = destination;
        this.path = path;
    }

    /**
     * Sets the request body
     * @param body the request body to match on exactly
     * @return the {@link RequestTemplateBuilder} for further customizations
     */
    public RequestTemplateBuilder body(final String body) {
        this.body = exactlyMatches(body);
        return this;
    }

    /**
     * Sets the request body using {@link HttpBodyConverter} to match on exactly
     * @param httpBodyConverter custom http body converter
     * @return the {@link RequestTemplateBuilder} for further customizations
     */
    @Deprecated
    public RequestTemplateBuilder body(HttpBodyConverter httpBodyConverter) {
        this.body = exactlyMatches(httpBodyConverter.body());
        return this;
    }

    public RequestTemplateBuilder body(RequestMatcher matcher) {
        this.body = matcher.getFieldMatcher();
        return this;
    }

    public RequestTemplateBuilder anyBody() {
        this.body = null;
        return this;
    }

    /**
     * Sets one request header
     * @param key the header key to match on
     * @param value the header value to match on
     * @return the {@link RequestTemplateBuilder} for further customizations
     */
    public RequestTemplateBuilder header(final String key, final String value) {
        headers.put(key, Collections.singletonList(value));
        return this;
    }

    /**
     * Sets the request query
     * @param key the query params key to match on
     * @param values the query params values to match on
     * @return the {@link RequestTemplateBuilder} for further customizations
     */
    public RequestTemplateBuilder queryParam(final String key, final Object... values) {
        for(Object value : values) {
            queryPatterns.add(ExactMatcher.newInstance(key),ExactMatcher.newInstance(value.toString()));
        }
        return this;
    }

    public RequestTemplateBuilder queryParam(final String key, final PlainTextMatcher value) {
        return queryParam(ExactMatcher.newInstance(key), value);
    }

    public RequestTemplateBuilder queryParam(final PlainTextMatcher key, final String value) {
        return queryParam(key, ExactMatcher.newInstance(value));
    }

    public RequestTemplateBuilder queryParam(final PlainTextMatcher key, final PlainTextMatcher value) {
        isFuzzyMatchedQuery = true;
        queryPatterns.add(key, value);
        return this;
    }



    public RequestTemplateBuilder anyQueryParams() {
        query = null;
        return this;
    }

    /**
     * Sets the expected response
     * @param responseBuilder the builder for response
     * @return the {@link StubServiceBuilder} for chaining the next {@link RequestTemplateBuilder}
     * @see ResponseBuilder
     */
    public StubServiceBuilder willReturn(final ResponseBuilder responseBuilder) {
        Request request = this.build();
        return invoker
                .addRequestResponsePair(new RequestResponsePair(request, responseBuilder.build()))
                .addDelaySetting(request, responseBuilder);
    }

    private Request build() {

        if (!this.queryPatterns.isEmpty()) {
            String queryPatterns = this.queryPatterns.entrySet().stream()
                    .flatMap(e -> e.getValue().stream().map(v -> encodeUrl(e.getKey().getPattern()) + "=" + encodeUrl(v.getPattern())))
                    .collect(Collectors.joining("&"));
            query = isFuzzyMatchedQuery ? wildCardMatches(queryPatterns) : exactlyMatches(queryPatterns);
        }

        return new Request(path, method, destination, scheme, query, body, headers);
    }

    private String encodeUrl(String str) {
        try {
            return URLEncoder.encode(str, UTF_8).replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
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
