package io.specto.hoverfly.junit.dsl.matchers;


import io.specto.hoverfly.junit.core.model.FieldMatcher;
import io.specto.hoverfly.junit.dsl.HoverflyDslException;
import org.json.JSONObject;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HoverflyMatchersTest {


    @Test
    public void matchesShouldCreateGlobMatcher() {

        PlainTextFieldMatcher matcher = HoverflyMatchers.matches("fo*o");

        assertThat(matcher).isInstanceOf(GlobMatcher.class);
        assertThat(matcher.getPattern()).isEqualTo("fo*o");
    }

    @Test
    public void startsWithShouldCreateRegexMatcher() {
        PlainTextFieldMatcher matcher = HoverflyMatchers.startsWith("foo");

        assertThat(matcher).isInstanceOf(RegexMatcher.class);
        assertThat(matcher.getPattern()).isEqualTo("^foo.*");
    }


    @Test
    public void endsWithShouldCreateRegexMatcher() {
        PlainTextFieldMatcher matcher = HoverflyMatchers.endsWith("foo");

        assertThat(matcher).isInstanceOf(RegexMatcher.class);
        assertThat(matcher.getPattern()).isEqualTo(".*foo$");
    }

    @Test
    public void containsShouldCreateRegexMatcher() {
        PlainTextFieldMatcher matcher = HoverflyMatchers.contains("foo");

        assertThat(matcher).isInstanceOf(RegexMatcher.class);
        assertThat(matcher.getPattern()).isEqualTo(".*foo.*");
    }

    @Test
    public void anyShouldCreateRegexMatcher() {
        PlainTextFieldMatcher matcher = HoverflyMatchers.any();

        assertThat(matcher).isInstanceOf(RegexMatcher.class);
        assertThat(matcher.getPattern()).isEqualTo(".*");
    }

    @Test
    public void shouldCreateJsonMatcherFromString() {
        FieldMatcher actual = HoverflyMatchers.equalsToJson("{\"flightId\":\"1\",\"class\":\"PREMIUM\"}").getFieldMatcher();

        FieldMatcher expected = new FieldMatcher.Builder().jsonMatch("{\"flightId\":\"1\",\"class\":\"PREMIUM\"}").build();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldThrowExceptionIfInputStringIsInvalidJsonFormat() {

        assertThatThrownBy(() -> HoverflyMatchers.equalsToJson("{\"flightId\":\"1\",\"class\":\"PREMIUM\""))
                .isInstanceOf(HoverflyDslException.class)
                .hasMessageContaining("Fail to create JSON matcher from invalid JSON string");

    }

    @Test
    public void shouldCreateJsonMatcherFromJSONObject() throws Exception {

        JSONObject object = new JSONObject().put("id", 1);

        FieldMatcher actual = HoverflyMatchers.equalsToJson(object.toString()).getFieldMatcher();

        FieldMatcher expected = new FieldMatcher.Builder().jsonMatch("{\"id\":1}").build();

        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void shouldCreateXmlMatcherFromString() {
        FieldMatcher actual = HoverflyMatchers.equalsToXml("<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <flightId>1</flightId> <class>PREMIUM</class>").getFieldMatcher();

        FieldMatcher expected = new FieldMatcher.Builder().xmlMatch("<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <flightId>1</flightId> <class>PREMIUM</class>").build();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldThrowExceptionIfInputStringIsInvalidXmlFormat() {

        assertThatThrownBy(() -> HoverflyMatchers.equalsToXml("{\"flightId\":\"1\",\"class\":\"PREMIUM\""))
                .isInstanceOf(HoverflyDslException.class)
                .hasMessageContaining("Fail to create XML matcher from invalid XML string");

    }
}