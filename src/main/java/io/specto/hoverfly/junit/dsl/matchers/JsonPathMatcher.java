package io.specto.hoverfly.junit.dsl.matchers;

import io.specto.hoverfly.junit.core.model.FieldMatcher;

/**
 * See <a href="https://github.com/NodePrime/jsonpath">NodePrime/jsonpath</a> for details of the Json Path supported.
 */

public class JsonPathMatcher implements RequestMatcher {

    private String pattern;
    private FieldMatcher fieldMatcher;


    private JsonPathMatcher(String pattern) {
        this.pattern = pattern;
        this.fieldMatcher = new FieldMatcher.Builder().jsonPathMatch(pattern).build();
    }

    static JsonPathMatcher createFromExpression(String expression) {
        return new JsonPathMatcher(expression);
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
