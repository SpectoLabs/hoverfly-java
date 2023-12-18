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
package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.specto.hoverfly.junit.core.ObjectMapperFactory;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {

    private List<RequestFieldMatcher<String>> path;

    private List<RequestFieldMatcher<String>> method;

    private List<RequestFieldMatcher<String>> destination;

    private List<RequestFieldMatcher<String>> scheme;

    private Map<String, List<RequestFieldMatcher>> query;

    private List<RequestFieldMatcher> body;

    private Map<String, List<RequestFieldMatcher>> headers;

    private Map<String, String> requiresState;

    public Request() {
    }

    public Request(List<RequestFieldMatcher<String>> path,
                   List<RequestFieldMatcher<String>> method,
                   List<RequestFieldMatcher<String>> destination,
                   List<RequestFieldMatcher<String>> scheme,
                   Map<String, List<RequestFieldMatcher>> query,
                   List<RequestFieldMatcher> body,
                   Map<String, List<RequestFieldMatcher>> headers,
                   Map<String, String> requiresState) {
        this.path = path;
        this.method = method;
        this.destination = destination;
        this.scheme = scheme;
        this.query = query;
        this.body = body;
        this.headers = headers;
        this.requiresState = requiresState;
    }

    public List<RequestFieldMatcher<String>> getPath() {
        return path;
    }

    public void setPath(List<RequestFieldMatcher<String>> path) {
        this.path = path;
    }

    public List<RequestFieldMatcher<String>> getMethod() {
        return method;
    }

    public void setMethod(List<RequestFieldMatcher<String>> method) {
        this.method = method;
    }

    public List<RequestFieldMatcher<String>> getDestination() {
        return destination;
    }

    public void setDestination(List<RequestFieldMatcher<String>> destination) {
        this.destination = destination;
    }

    public List<RequestFieldMatcher<String>> getScheme() {
        return scheme;
    }

    public void setScheme(List<RequestFieldMatcher<String>> scheme) {
        this.scheme = scheme;
    }

    public Map<String, List<RequestFieldMatcher>> getQuery() {
        return query;
    }

    public void setQuery(Map<String, List<RequestFieldMatcher>> query) {
        this.query = query;
    }

    public List<RequestFieldMatcher> getBody() {
        return body;
    }

    public void setBody(List<RequestFieldMatcher> body) {
        this.body = body;
    }

    public Map<String, List<RequestFieldMatcher>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<RequestFieldMatcher>> headers) {
        this.headers = headers;
    }

    public Map<String, String> getRequiresState() {
        return requiresState;
    }

    public void setRequiresState(Map<String, String> requiresState) {
        this.requiresState = requiresState;
    }

    public static class Builder {

        private List<RequestFieldMatcher<String>> path;
        private List<RequestFieldMatcher<String>> method;
        private List<RequestFieldMatcher<String>> destination;
        private List<RequestFieldMatcher<String>> scheme;
        private Map<String, List<RequestFieldMatcher>> query;
        private List<RequestFieldMatcher> body;
        private Map<String, List<RequestFieldMatcher>> headers;
        private Map<String, String> requiresState;

        public Request.Builder path(List<RequestFieldMatcher<String>> path) {
            this.path = path;
            return this;
        }

        public Request.Builder method(List<RequestFieldMatcher<String>> method) {
            this.method = method;
            return this;
        }

        public Request.Builder destination(List<RequestFieldMatcher<String>> destination) {
            this.destination = destination;
            return this;
        }

        public Request.Builder scheme(List<RequestFieldMatcher<String>> scheme) {
            this.scheme = scheme;
            return this;
        }

        public Request.Builder query(Map<String, List<RequestFieldMatcher>> query) {
            this.query = query;
            return this;
        }

        public Request.Builder body(List<RequestFieldMatcher> body) {
            this.body = body;
            return this;
        }

        public Request.Builder headers(Map<String, List<RequestFieldMatcher>> headers) {
            this.headers = headers;
            return this;
        }

        public Request.Builder requiresState(Map<String, String> requiresState) {
            this.requiresState = requiresState;
            return this;
        }

        public Request build() {
            return new Request(path, method, destination, scheme, query, body, headers, requiresState);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        try {
            return ObjectMapperFactory.getPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return ToStringBuilder.reflectionToString(this);
        }
    }


}
