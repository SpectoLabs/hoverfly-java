package io.specto.hoverfly.junit.dsl.matchers;

import io.specto.hoverfly.junit.core.model.FieldMatcher;

public class ExactMatcher implements PlainTextFieldMatcher {

    private String pattern;
    private FieldMatcher fieldMatcher;

    private ExactMatcher(String pattern) {
        this.pattern = pattern;
        this.fieldMatcher = FieldMatcher.exactlyMatches(pattern);
    }


    static ExactMatcher newInstance(Object value) {
        return new ExactMatcher(value.toString());
    }

    @Override
    public FieldMatcher getFieldMatcher() {
        return fieldMatcher;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public RequestMatcherType getType() {
        return RequestMatcherType.EXACT_MATCH;
    }
}
