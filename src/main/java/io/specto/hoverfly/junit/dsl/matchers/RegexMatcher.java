package io.specto.hoverfly.junit.dsl.matchers;

import io.specto.hoverfly.junit.core.model.FieldMatcher;

class RegexMatcher implements PlainTextFieldMatcher {

    private FieldMatcher fieldMatcher;

    private String pattern;

    private RegexMatcher(String pattern) {
        this.pattern = pattern;
        this.fieldMatcher = new FieldMatcher.Builder().regexMatch(pattern).build();
    }

    static RegexMatcher newInstance(String pattern) {
        return new RegexMatcher(pattern);
    }

    @Override
    public FieldMatcher getFieldMatcher() {
        return this.fieldMatcher;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public RequestMatcherType getType() {
        return RequestMatcherType.REGEX_MATCH;
    }
}
