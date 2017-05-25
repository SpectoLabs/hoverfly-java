package io.specto.hoverfly.junit.dsl.matchers;

public class HoverflyMatchers {

    public static PlainTextMatcher matches(String value) {
        return GlobMatcher.createFromPattern(value);
    }

    public static PlainTextMatcher startsWith(String value) {
        return GlobMatcher.createFromStringFormat("%s*", value);
    }

    public static RequestMatcher equalsToJson(String value) {
        return new JsonMatcher();
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
