package io.specto.hoverfly.junit.dsl.matchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.specto.hoverfly.junit.core.model.FieldMatcher;
import io.specto.hoverfly.junit.dsl.HoverflyDslException;
import io.specto.hoverfly.junit.dsl.HttpBodyConverter;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.io.IOException;

public class HoverflyMatchers {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final XmlMapper XML_MAPPER = new XmlMapper();

    private HoverflyMatchers() { }

    /**
     * Create a matcher that exactly equals to the String value of the given object
     * @param value the value to match on
     * @return an {@link ExactMatcher}
     */
    public static PlainTextFieldMatcher equalsTo(Object value) {
        return ExactMatcher.newInstance(value);
    }

    /**
     * Create a matcher that matches a GLOB pattern.
     * For example:
     * <pre>HoverflyMatchers.matches("api-v*.test-svc.*")</pre>
     * @param value the GLOB pattern, use the wildcard character '*' to match any characters
     * @return an {@link GlobMatcher}
     */
    public static PlainTextFieldMatcher matches(String value) {
        return GlobMatcher.newInstance(value);
    }

    /**
     * Create a matcher that matches a Golang regex pattern.
     * As the Hoverfly core project is written in Golang, this method is provided as a temporary solution to use the
     * regex matcher using native Golang regex patterns.
     * Although there are some variations from the Java regex, majority of the syntax is similar.
     * @see <a href="https://regex-golang.appspot.com/assets/html/index.html">Golang regex quick reference</a>
     * @param regexPattern the Golang regex pattern
     * @return an {@link RegexMatcher}
     */
    public static PlainTextFieldMatcher matchesGoRegex(String regexPattern) {
        return RegexMatcher.newInstance(regexPattern);
    }

    /**
     * Create a matcher that matches on a string prefixed with the given value
     * @param value the value to start with
     * @return an {@link RegexMatcher}
     */
    public static PlainTextFieldMatcher startsWith(String value) {
        return RegexMatcher.newInstance(String.format("^%s.*", value));
    }

    /**
     * Create a matcher that matches on a string post-fixed with the given value
     * @param value the value to end with
     * @return an {@link RegexMatcher}
     */
    public static PlainTextFieldMatcher endsWith(String value) {
        return RegexMatcher.newInstance(String.format(".*%s$", value));
    }

    /**
     * Create a matcher that matches on a string containing the given value
     * @param value the value to contain
     * @return an {@link RegexMatcher}
     */
    public static PlainTextFieldMatcher contains(String value) {
        return RegexMatcher.newInstance(String.format(".*%s.*", value));
    }

    /**
     * Create a matcher that matches on any value
     * @return an {@link RegexMatcher}
     */
    public static PlainTextFieldMatcher any() {
        return RegexMatcher.newInstance(".*");
    }

    /**
     * Create a matcher that matches on the given JSON
     * @param value the JSON string value
     * @return an {@link RequestFieldMatcher} that includes jsonMatch
     */
    public static RequestFieldMatcher equalsToJson(String value) {
        validateJson(value);
        return () -> new FieldMatcher.Builder().jsonMatch(value).build();
    }

    /**
     * Create a matcher that matches on JSON serialized from a JAVA object by {@link HttpBodyConverter}
     * @param converter the {@link HttpBodyConverter} with an object to be serialized to JSON
     * @return an {@link RequestFieldMatcher} that includes jsonMatch
     */
    public static RequestFieldMatcher equalsToJson(HttpBodyConverter converter) {
        return equalsToJson(converter.body());
    }

    /**
     * Create a matcher that matches on the given JsonPath expression
     * @param expression the JsonPath expression
     * @return an {@link RequestFieldMatcher} that includes jsonPathMatch
     */
    public static RequestFieldMatcher matchesJsonPath(String expression) {
        return () -> new FieldMatcher.Builder().jsonPathMatch(expression).build();
    }

    /**
     * Create a matcher that matches on the given XML
     * @param value the XML string value
     * @return an {@link RequestFieldMatcher} that includes xmlMatch
     */
    public static RequestFieldMatcher equalsToXml(String value) {
        validateXml(value);
        return () -> new FieldMatcher.Builder().xmlMatch(value).build();
    }

    /**
     * Create a matcher that matches on XML serialized from a JAVA object by {@link HttpBodyConverter}
     * @param converter the {@link HttpBodyConverter} with an object to be serialized to XML
     * @return an {@link RequestFieldMatcher} that includes xmlMatch
     */
    public static RequestFieldMatcher equalsToXml(HttpBodyConverter converter) {
        return equalsToXml(converter.body());
    }

    /**
     * Create a matcher that matches on the given XPath expression
     * @param expression the XPath expression
     * @return an {@link RequestFieldMatcher} that includes xpathMatch
     */
    public static RequestFieldMatcher matchesXPath(String expression) {
        return () -> new FieldMatcher.Builder().xpathMatch(expression).build();
    }


    private static void validateJson(String value) {
        try {
            OBJECT_MAPPER.readTree(value);
        } catch (IOException e) {
            throw new HoverflyDslException("Fail to create JSON matcher from invalid JSON string: " + value);
        }
    }

    private static void validateXml(String value) {
        try {
            XML_MAPPER.readTree(value);
        } catch (IOException e) {
            throw new HoverflyDslException("Fail to create XML matcher from invalid XML string: " + value);
        }
    }

}
