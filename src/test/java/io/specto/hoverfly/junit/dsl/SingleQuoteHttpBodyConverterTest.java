package io.specto.hoverfly.junit.dsl;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SingleQuoteHttpBodyConverterTest {

    @Test
    public void shouldConvertSingleQuotes() {
        final SingleQuoteHttpBodyConverter singleQuoteHttpBodyConverter =
            SingleQuoteHttpBodyConverter.jsonWithSingleQuotes("{'bookingId':'1'}");

        assertThat(singleQuoteHttpBodyConverter.body())
            .isEqualTo("{\"bookingId\":\"1\"}");
    }

    @Test
    public void shouldSkipDoubleQuotes() {
        final SingleQuoteHttpBodyConverter singleQuoteHttpBodyConverter =
            SingleQuoteHttpBodyConverter.jsonWithSingleQuotes("{\"bookingId\":\"1\"}");

        assertThat(singleQuoteHttpBodyConverter.body())
            .isEqualTo("{\"bookingId\":\"1\"}");
    }

}
