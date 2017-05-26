package io.specto.hoverfly.junit.dsl.matchers;

import io.specto.hoverfly.junit.core.model.FieldMatcher;

public class XPathMatcher implements RequestMatcher {

    private String pattern;
    private FieldMatcher fieldMatcher;

    private XPathMatcher(String pattern) {
        this.pattern = pattern;
        this.fieldMatcher = new FieldMatcher.Builder().xpathMatch(pattern).build();
    }

    @Override
    public FieldMatcher getFieldMatcher() {
        return fieldMatcher;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    static XPathMatcher createFromExpression(String expression) {
        return new XPathMatcher(expression);
    }
}
