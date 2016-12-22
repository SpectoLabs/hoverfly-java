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
package io.specto.hoverfly.junit.dsl;

import io.specto.hoverfly.junit.core.model.RequestMatcher;
import io.specto.hoverfly.junit.core.model.RequestResponsePair;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

/**
 * A builder for {@link RequestMatcher}
 */
public class RequestMatcherBuilder {

    private static final String TEMPLATE = "template";

    private final StubServiceBuilder invoker;
    private final String method;
    private final String scheme;
    private final String destination;
    private final String path;
    private final MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
    private final Map<String, List<String>> headers = new HashMap<>();
    private String body;
    private String query;

    private RequestMatcherBuilder(final StubServiceBuilder invoker, final String method, final String scheme, final String destination, final String path) {
        this.invoker = invoker;
        this.method = method;
        this.scheme = scheme;
        this.destination = destination;
        this.path = path;
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
        this.body = body;
        return this;
    }

    /**
     * Sets the request query
     * @param query the query params string to match on
     * @return the {@link RequestMatcherBuilder} for further customizations
     */
    public RequestMatcherBuilder query(final String query) {
        this.query = query;
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
     * Sets the expected response
     * @param responseBuilder the builder for response
     * @return the {@link StubServiceBuilder} for chaining the next {@link RequestMatcherBuilder}
     * @see ResponseBuilder
     */
    public StubServiceBuilder willReturn(final ResponseBuilder responseBuilder) {
        return invoker.addRequestResponsePair(new RequestResponsePair(this.build(), responseBuilder.build()));
    }

//    private RequestMatcherBuilder queryParam(final String key, final Object... values) {
//        for(Object value : values) {
//            queryParams.add(key, value.toString());
//        }
//        return this;
//    }

    private RequestMatcher build() {
        // TODO Hoverfly only supports exact request query matching at the moment, will enable queryParams builder when the problem is resolved
//        query = queryParams.entrySet().stream()
//                .flatMap(e -> e.getValue().stream().map(v -> encodeUrl(e.getKey()) + "=" + encodeUrl(v)))
//                .collect(Collectors.joining("&"));
        return new RequestMatcher(path, method, destination, scheme, query, body, headers, TEMPLATE);
    }


    private String encodeUrl(String str) {
        try {
            return URLEncoder.encode(str, UTF_8).replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public RequestMatcherBuilder body(BodyConverter bodyConverter) {
        this.body = bodyConverter.body();
        header("Content-Type", bodyConverter.contentType());
        return this;
    }
}
