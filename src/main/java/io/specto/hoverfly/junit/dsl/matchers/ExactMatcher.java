package io.specto.hoverfly.junit.dsl.matchers;

import io.specto.hoverfly.junit.core.model.FieldMatcher;

public class ExactMatcher implements PlainTextMatcher {

    private String pattern;
    private FieldMatcher fieldMatcher;

    private ExactMatcher(String pattern) {
        this.pattern = pattern;
        this.fieldMatcher = FieldMatcher.exactlyMatches(pattern);
    }


    public static ExactMatcher newInstance(String value) {
        return new ExactMatcher(value);
    }

    @Override
    public FieldMatcher getFieldMatcher() {
        return fieldMatcher;
    }

    @Override
    public String getPattern() {
        return pattern;
    }
}
