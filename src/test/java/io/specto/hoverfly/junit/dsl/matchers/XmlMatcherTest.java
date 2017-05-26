package io.specto.hoverfly.junit.dsl.matchers;


import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.specto.hoverfly.junit.core.model.FieldMatcher;
import io.specto.hoverfly.junit.dsl.HoverflyDslException;
import io.specto.hoverfly.models.SimpleBooking;
import org.junit.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class XmlMatcherTest {


    @Test
    public void shouldCreateXmlMatcherFromString() throws Exception {
        FieldMatcher actual = XmlMatcher.createFromString("<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <flightId>1</flightId> <class>PREMIUM</class>").getFieldMatcher();

        FieldMatcher expected = new FieldMatcher.Builder().xmlMatch("<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <flightId>1</flightId> <class>PREMIUM</class>").build();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldThrowExceptionIfInputStringIsInvalidXmlFormat() throws Exception {

        assertThatThrownBy(() -> XmlMatcher.createFromString("{\"flightId\":\"1\",\"class\":\"PREMIUM\""))
                .isInstanceOf(HoverflyDslException.class)
                .hasMessageContaining("Fail to create JsonMatcher from invalid Xml string");

    }

    @Test
    public void shouldCreateXmlMatcherFromObject() throws Exception {
        SimpleBooking booking = new SimpleBooking(1, "London", "Hong Kong", LocalDate.now());
        FieldMatcher actual = XmlMatcher.createFromObject(booking).getFieldMatcher();

        FieldMatcher expected = new FieldMatcher.Builder().xmlMatch(new XmlMapper().writeValueAsString(booking)).build();

        assertThat(actual).isEqualTo(expected);
    }
}