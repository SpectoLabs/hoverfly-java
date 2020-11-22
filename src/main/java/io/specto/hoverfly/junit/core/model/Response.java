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
import sun.rmi.runtime.Log;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Response {
    private final Integer status;
    private final String body;
    private final boolean encodedBody;
    private final boolean templated;
    private final Map<String, List<String>> headers;
    private final Map<String, String> transitionsState;
    private final List<String> removesState;
    private final Integer fixedDelay;
    private final LogNormalDelay logNormalDelay;

    @JsonCreator
    public Response(
            @JsonProperty("status") Integer status,
            @JsonProperty("body") String body,
            @JsonProperty("encodedBody") boolean encodedBody,
            @JsonProperty("templated") boolean templated,
            @JsonProperty("headers") Map<String, List<String>> headers,
            @JsonProperty("transitionsState") Map<String, String> transitionsState,
            @JsonProperty("removesState") List<String> removesState,
            @JsonProperty("fixedDelay") Integer fixedDelay,
            @JsonProperty("logNormalDelay") LogNormalDelay logNormalDelay) {
        this.status = status;
        this.body = body;
        this.encodedBody = encodedBody;
        this.templated = templated;
        this.headers = headers;
        this.transitionsState = transitionsState;
        this.removesState = removesState;
        this.fixedDelay = fixedDelay;
        this.logNormalDelay = logNormalDelay;
    }

    public Integer getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    public boolean isEncodedBody() {
        return encodedBody;
    }


    public boolean isTemplated() {
        return templated;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public Map<String, String> getTransitionsState() {
        return transitionsState;
    }

    public List<String> getRemovesState() {
        return removesState;
    }

    public Integer getFixedDelay() {
        return fixedDelay;
    }

    public LogNormalDelay getLogNormalDelay() {
        return logNormalDelay;
    }

    static class Builder {
        private Integer status;
        private String body;
        private boolean encodedBody;
        private boolean templated;
        private Map<String, List<String>> headers;
        private Map<String, String> transitionsState;
        private List<String> removesState;
        private Integer fixedDelay;
        private LogNormalDelay logNormalDelay;

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

        Builder templated(boolean templated) {
            this.templated = templated;
            return this;
        }

        Builder headers(Map<String, List<String>> headers) {
            this.headers = headers;
            return this;
        }

        Builder transitionsState(Map<String, String> transitionsState) {
            this.transitionsState = transitionsState;
            return this;
        }

        Builder removesState(List<String> removesState) {
            this.removesState = removesState;
            return this;
        }

        Builder fixedDelay(int fixedDelay) {
            this.fixedDelay = fixedDelay;
            return this;
        }

        Builder logNormalDelay(int min, int max, int mean, int median) {
            this.logNormalDelay = new LogNormalDelay(min, max, mean, median);
            return this;
        }

        Response build() {
            return new Response(status, body, encodedBody, templated, headers, transitionsState, removesState, fixedDelay, logNormalDelay);
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
