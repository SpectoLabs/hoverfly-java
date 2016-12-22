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
package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;
import java.util.Map;

public class RequestMatcher {
    private final String requestType;
    private final String path;
    private final String method;
    private final String destination;
    private final String scheme;
    private final String query;
    private final String body;
    private final Map<String, List<String>> headers;

    @JsonCreator
    public RequestMatcher(@JsonProperty("path") String path,
                          @JsonProperty("method") String method,
                          @JsonProperty("destination") String destination,
                          @JsonProperty("scheme") String scheme,
                          @JsonProperty("query") String query,
                          @JsonProperty("body") String body,
                          @JsonProperty("headers") Map<String, List<String>> headers,
                          @JsonProperty("requestType") String requestType) {
        this.path = path;
        this.method = method;
        this.destination = destination;
        this.scheme = scheme;
        this.query = query;
        this.body = body;
        this.headers = headers;
        this.requestType = requestType;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public String getDestination() {
        return destination;
    }

    public String getScheme() {
        return scheme;
    }

    public String getQuery() {
        return query;
    }

    public String getBody() {
        return body;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}