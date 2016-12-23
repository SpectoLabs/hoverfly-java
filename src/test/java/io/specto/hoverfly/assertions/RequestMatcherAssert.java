package io.specto.hoverfly.assertions;

import io.specto.hoverfly.junit.core.model.RequestMatcher;
import org.assertj.core.api.AbstractAssert;

import java.util.Arrays;
import java.util.Set;

import static io.specto.hoverfly.assertions.Header.header;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

public class RequestMatcherAssert extends AbstractAssert<RequestMatcherAssert, RequestMatcher> {
    public RequestMatcherAssert(final RequestMatcher actual) {
        super(actual, RequestMatcherAssert.class);
    }

    public RequestMatcherAssert hasNoBody() {
        isNotNull();

        assertThat(actual.getBody()).isEmpty();

        return this;
    }

    public RequestMatcherAssert hasBody(final String body) {
        isNotNull();

        assertThat(actual.getBody()).isEqualTo(body);

        return this;
    }

    public RequestMatcherAssert hasExactHeaders(final Header... headers) {
        isNotNull();

        final Set<Header> setOfHeaders = Arrays.stream(headers)
                .map(h -> header(h.getKey(), h.getValue()))
                .collect(toSet());

        assertThat(actual.getHeaders()).containsOnly(setOfHeaders.toArray(new Header[setOfHeaders.size()]));

        return this;
    }

    public RequestMatcherAssert hasNoHeaders() {
        isNotNull();

        assertThat(actual.getHeaders()).isEmpty();

        return this;
    }
}
