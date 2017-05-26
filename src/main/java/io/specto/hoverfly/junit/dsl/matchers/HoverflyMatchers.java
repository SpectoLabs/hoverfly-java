package io.specto.hoverfly.junit.dsl.matchers;

public class HoverflyMatchers {

    public static PlainTextMatcher matches(String value) {
        return GlobMatcher.createFromPattern(value);
    }

    public static PlainTextMatcher startsWith(String value) {
        return GlobMatcher.createFromStringFormat("%s*", value);
    }

    public static RequestMatcher equalsToJson(String value) {
        return JsonMatcher.createFromString(value);
    }

    public static RequestMatcher equalsToJson(Object value) {
        return JsonMatcher.createFromObject(value);
    }

    public static RequestMatcher matchesJsonPath(String expression) {
        return JsonPathMatcher.createFromExpression(expression);
    }

    public static RequestMatcher equalsToXml(String value) {
        return XmlMatcher.createFromString(value);
    }

    public static RequestMatcher equalsToXml(Object value) {
        return XmlMatcher.createFromObject(value);
    }

    public static RequestMatcher matchesXPath(String expression) {
        return XPathMatcher.createFromExpression(expression);
    }

    public static PlainTextMatcher endsWith(String value) {
        return GlobMatcher.createFromStringFormat("*%s", value);
    }

    // TODO this pattern doesn't work if the target string starts with or ends with the string to match
    public static PlainTextMatcher contains(String value) {
        return GlobMatcher.createFromStringFormat("*%s*", value);

    }

    public static PlainTextMatcher any() {
        return GlobMatcher.createFromPattern("*");
    }
}
