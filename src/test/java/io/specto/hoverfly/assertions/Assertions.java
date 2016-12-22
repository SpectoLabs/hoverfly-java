package io.specto.hoverfly.assertions;

import io.specto.hoverfly.junit.core.model.Response;

public class Assertions extends org.assertj.core.api.Assertions {
    public static ResponseAssert assertThat(final Response response) {
        return new ResponseAssert(response);
    }
}
