package io.specto.hoverfly.junit.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class RequestResponsePairView {
    private final RequestDetails request;
    private final ResponseDetails response;

    @JsonCreator
    public RequestResponsePairView(@JsonProperty("request") RequestDetails request,
                                   @JsonProperty("response") ResponseDetails response) {
        this.request = request;
        this.response = response;
    }

    public RequestDetails getRequest() {
        return request;
    }

    public ResponseDetails getResponse() {
        return response;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RequestResponsePairView that = (RequestResponsePairView) o;

        return new EqualsBuilder()
                .append(request, that.request)
                .append(response, that.response)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(request)
                .append(response)
                .toHashCode();
    }
}