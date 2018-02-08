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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.specto.hoverfly.junit.core.model.FieldMatcher.fromString;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Request {

    @JsonDeserialize(using = FieldMatcherDeserializer.class)
    private final FieldMatcher path;
    @JsonDeserialize(using = FieldMatcherDeserializer.class)
    private final FieldMatcher method;
    @JsonDeserialize(using = FieldMatcherDeserializer.class)
    private final FieldMatcher destination;
    @JsonDeserialize(using = FieldMatcherDeserializer.class)
    private final FieldMatcher scheme;
    @JsonDeserialize(using = FieldMatcherDeserializer.class)
    private final FieldMatcher query;
    @JsonDeserialize(using = FieldMatcherDeserializer.class)
    private final FieldMatcher body;
    private final Map<String, List<String>> headers;
    private final Map<String, String> requiresState;

    private RequestType requestType;

    @Deprecated
    public Request(String path,
                   String method,
                   String destination,
                   String scheme,
                   String query,
                   String body,
                   Map<String, List<String>> headers,
                   Map<String, String> requiresState) {
        this.path = fromString(path);
        this.method = fromString(method);
        this.destination = fromString(destination);
        this.scheme = fromString(scheme);
        this.query = fromString(query);
        this.body = fromString(body);
        this.headers = headers;
        this.requiresState = requiresState;
    }


    @JsonCreator
    public Request(@JsonProperty("path") FieldMatcher path,
                   @JsonProperty("method") FieldMatcher method,
                   @JsonProperty("destination") FieldMatcher destination,
                   @JsonProperty("scheme") FieldMatcher scheme,
                   @JsonProperty("query") FieldMatcher query,
                   @JsonProperty("body") FieldMatcher body,
                   @JsonProperty("headers") Map<String, List<String>> headers,
                   @JsonProperty("requiresState") Map<String, String> requiresState) {
        this.path = path;
        this.method = method;
        this.destination = destination;
        this.scheme = scheme;
        this.query = query;
        this.body = body;
        this.headers = headers;
        this.requiresState = requiresState;
    }

    public FieldMatcher getPath() {
        return path;
    }

    public FieldMatcher getMethod() {
        return method;
    }

    public FieldMatcher getDestination() {
        return destination;
    }

    public FieldMatcher getScheme() {
        return scheme;
    }

    public FieldMatcher getQuery() {
        return query;
    }

    public FieldMatcher getBody() {
        return body;
    }

    public Map<String, List<String>> getHeaders() {
        return requestType == RequestType.RECORDING ? Collections.emptyMap() : headers;
    }


    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public Map<String, String> getRequiresState() {
        return requiresState;
    }

    public static class Builder {

        private FieldMatcher path;
        private FieldMatcher method;
        private FieldMatcher destination;
        private FieldMatcher scheme;
        private FieldMatcher query;
        private FieldMatcher body;
        private Map<String, List<String>> headers;
        private Map<String, String> requiresState;

        public Builder path(FieldMatcher path) {
            this.path = path;
            return this;
        }

        public Builder method(FieldMatcher method) {
            this.method = method;
            return this;
        }

        public Builder destination(FieldMatcher destination) {
            this.destination = destination;
            return this;
        }

        public Builder scheme(FieldMatcher scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder query(FieldMatcher query) {
            this.query = query;
            return this;
        }

        public Builder body(FieldMatcher body) {
            this.body = body;
            return this;
        }

        public Builder headers(Map<String, List<String>> headers) {
            this.headers = headers;
            return this;
        }

        public Builder requiresState(Map<String, String> requiresState) {
            this.requiresState = requiresState;
            return this;
        }

        public Request build() {
            return new Request(path, method, destination, scheme, query, body, headers, requiresState);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, "requestType");
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, "requestType");
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return ToStringBuilder.reflectionToString(this);
        }
    }

    enum RequestType {
        @JsonProperty("template")
        TEMPLATE,
        @JsonProperty("recording")
        RECORDING
    }
}