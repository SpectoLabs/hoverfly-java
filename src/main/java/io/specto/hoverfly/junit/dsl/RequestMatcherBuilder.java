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
import io.specto.hoverfly.junit.core.model.RequestMatcher;
import io.specto.hoverfly.junit.core.model.RequestResponsePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.specto.hoverfly.junit.core.model.FieldMatcher.fromString;
import static org.apache.commons.lang3.CharEncoding.UTF_8;

/**
 * A builder for {@link RequestMatcher}
 */
public class RequestMatcherBuilder {

    private final StubServiceBuilder invoker;
    private final FieldMatcher method;
    private final FieldMatcher scheme;
    private final FieldMatcher destination;
    private final FieldMatcher path;
    private final MultivaluedHashMap<String, String> queryParams = new MultivaluedHashMap<>();
    private final Map<String, List<String>> headers = new HashMap<>();
    private FieldMatcher body;

    private RequestMatcherBuilder(final StubServiceBuilder invoker, final String method, final String scheme, final String destination, final String path) {
        this.invoker = invoker;
        this.method = fromString(method);
        this.scheme = fromString(scheme);
        this.destination = fromString(destination);
        this.path = fromString(path);
    }

    static RequestMatcherBuilder requestMatcherBuilder(final StubServiceBuilder invoker, final String method, final String scheme, final String destination, final String path) {
        return new RequestMatcherBuilder(invoker, method, scheme, destination, path);
    }


    /**
     * Sets the request body
     * @param body the request body to match on
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder body(final String body) {
        this.body = fromString(body);
        return this;
    }

    /**
     * Sets the request body and the matching content-type header using {@link HttpBodyConverter}
     * @param httpBodyConverter custom http body converter
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder body(HttpBodyConverter httpBodyConverter) {
        this.body = fromString(httpBodyConverter.body());
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
        for(Object value : values) {
            queryParams.add(key, value.toString());
        }
        return this;
    }

    /**
     * Sets the expected response
     * @param responseBuilder the builder for response
     * @return the {@link StubServiceBuilder} for chaining the next {@link RequestMatcherBuilder}
     * @see ResponseBuilder
     */
    public StubServiceBuilder willReturn(final ResponseBuilder responseBuilder) {
        RequestMatcher requestMatcher = this.build();
        return invoker
                .addRequestResponsePair(new RequestResponsePair(requestMatcher, responseBuilder.build()))
                .addDelaySetting(requestMatcher, responseBuilder);
    }

    private RequestMatcher build() {
        String query = queryParams.entrySet().stream()
                .flatMap(e -> e.getValue().stream().map(v -> encodeUrl(e.getKey()) + "=" + encodeUrl(v)))
                .collect(Collectors.joining("&"));
        return new RequestMatcher(path, method, destination, scheme, fromString(query), body, headers);
    }

    private String encodeUrl(String str) {
        try {
            return URLEncoder.encode(str, UTF_8).replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static class MultivaluedHashMap<K, V> {
        private Map<K, List<V>> elements = new HashMap<>();

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

    }

}
