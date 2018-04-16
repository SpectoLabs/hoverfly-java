package io.specto.hoverfly.assertions;

import io.specto.hoverfly.junit.core.model.Request;
import org.assertj.core.api.AbstractAssert;

import java.util.Arrays;
import java.util.Set;

import static io.specto.hoverfly.assertions.Header.header;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

public class RequestMatcherAssert extends AbstractAssert<RequestMatcherAssert, Request> {
    RequestMatcherAssert(final Request actual) {
        super(actual, RequestMatcherAssert.class);
    }

    public RequestMatcherAssert hasBody(final String body) {
        isNotNull();

        assertThat(actual.getBody().getExactMatch()).isEqualTo(body);

        return this;
    }

}
