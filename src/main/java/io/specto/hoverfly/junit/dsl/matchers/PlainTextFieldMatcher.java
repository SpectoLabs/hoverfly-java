package io.specto.hoverfly.junit.dsl.matchers;

public interface PlainTextFieldMatcher extends RequestFieldMatcher {

    String getPattern();

    RequestMatcherType getType();

}
