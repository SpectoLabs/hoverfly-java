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

public class Response {
    private final int status;
    private final String body;
    private final boolean encodedBody;
    private final Map<String, List<String>> headers;

    @JsonCreator
    public Response(@JsonProperty("status") int status,
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

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}