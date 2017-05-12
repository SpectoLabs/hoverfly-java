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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Response {
    private final Integer status;
    private final String body;
    private final boolean encodedBody;
    private final Map<String, List<String>> headers;

    @JsonCreator
    public Response(@JsonProperty("status") Integer status,
                    @JsonProperty("body") String body,
                    @JsonProperty("encodedBody") boolean encodedBody,
                    @JsonProperty("headers") Map<String, List<String>> headers) {
        this.status = status;
        this.body = body;
        this.encodedBody = encodedBody;
        this.headers = headers;
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    public boolean isEncodedBody() {
        return encodedBody;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    static class Builder {
        private Integer status = null;
        private String body = null;
        private boolean encodedBody = false;
        private Map<String, List<String>> headers = null;

        Builder status(int status) {
            this.status = status;
            return this;
        }

        Builder body(String body) {
            this.body = body;
            return this;
        }

        Builder encodedBody(boolean encodedBody) {
            this.encodedBody = encodedBody;
            return this;
        }

        Builder headers(Map<String, List<String>> headers) {
            this.headers = headers;
            return this;
        }

        Response build() {
            return new Response(status, body, encodedBody, headers);
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
        return ToStringBuilder.reflectionToString(this);
    }
}