package io.specto.hoverfly.junit.dsl.matchers;

import io.specto.hoverfly.junit.core.model.FieldMatcher;

class GlobMatcher implements PlainTextMatcher {

    private FieldMatcher fieldMatcher;

    private String pattern;

    private GlobMatcher(String pattern) {
        this.pattern = pattern;
        this.fieldMatcher = FieldMatcher.wildCardMatches(pattern);
    }

    static GlobMatcher createFromPattern(String pattern) {
        return new GlobMatcher(pattern);
    }

    static GlobMatcher createFromStringFormat(String format, String value) {
        String pattern = String.format(format, value);
        return new GlobMatcher(pattern);
    }


    @Override
    public FieldMatcher getFieldMatcher() {
        return this.fieldMatcher;
    }

    @Override
    public String getPattern() {
        return pattern;
    }
}
