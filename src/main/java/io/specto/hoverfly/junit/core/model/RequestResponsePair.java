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

public class RequestResponsePair {
    private final RequestMatcher request;
    private final Response response;

    @JsonCreator
    public RequestResponsePair(@JsonProperty("request") RequestMatcher request,
                               @JsonProperty("response") Response response) {
        this.request = request;
        this.response = response;
    }

    public RequestMatcher getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, "response");
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, "response");
    }
}