package io.specto.hoverfly.junit.dsl.matchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.specto.hoverfly.junit.core.model.RequestFieldMatcher;
import io.specto.hoverfly.junit.dsl.HoverflyDslException;
import io.specto.hoverfly.junit.dsl.HttpBodyConverter;

import java.io.IOException;

public class HoverflyMatchers {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final XmlMapper XML_MAPPER = new XmlMapper();

    private HoverflyMatchers() { }

    /**
     * Create a matcher that exactly equals to the String value of the given object
     * @param value the value to match on
     * @return an {@link RequestFieldMatcher}
     */
    public static RequestFieldMatcher equalsTo(Object value) {
        return RequestFieldMatcher.newExactMatcher(value.toString());
    }

    /**
     * Create a matcher that matches a GLOB pattern.
     * For example:
     * <pre>HoverflyMatchers.matches("api-v*.test-svc.*")</pre>
     * @param value the GLOB pattern, use the wildcard character '*' to match any characters
     * @return an {@link RequestFieldMatcher}
     */
    public static RequestFieldMatcher matches(String value) {
        return RequestFieldMatcher.newGlobMatcher(value);
    }

    /**
     * Create a matcher that matches a Golang regex pattern.
     * As the Hoverfly core project is written in Golang, this method is provided as a temporary solution to use the
     * regex matcher using native Golang regex patterns.
     * Although there are some variations from the Java regex, majority of the syntax is similar.
     * @see <a href="https://regex-golang.appspot.com/assets/html/index.html">Golang regex quick reference</a>
     * @param regexPattern the Golang regex pattern
     * @return an {@link RequestFieldMatcher}
     */
    public static RequestFieldMatcher matchesGoRegex(String regexPattern) {
        return RequestFieldMatcher.newRegexMatcher(regexPattern);
    }

    /**
     * Create a matcher that matches on a string prefixed with the given value
     * @param value the value to start with
     * @return an {@link RequestFieldMatcher}
     */
    public static RequestFieldMatcher startsWith(String value) {
        return RequestFieldMatcher.newRegexMatcher(String.format("^%s.*", value));
    }

    /**
     * Create a matcher that matches on a string post-fixed with the given value
     * @param value the value to end with
     * @return an {@link RequestFieldMatcher}
     */
    public static RequestFieldMatcher endsWith(String value) {
        return RequestFieldMatcher.newRegexMatcher(String.format(".*%s$", value));
    }

    /**
     * Create a matcher that matches on a string containing the given value
     * @param value the value to contain
     * @return an {@link RequestFieldMatcher}
     */
    public static RequestFieldMatcher contains(String value) {
        return RequestFieldMatcher.newRegexMatcher(String.format(".*%s.*", value));
    }

    /**
     * Create a matcher that matches on any value
     * @return an {@link RequestFieldMatcher}
     */
    public static RequestFieldMatcher any() {
        return RequestFieldMatcher.newRegexMatcher(".*");
    }

    /**
     * Create a matcher that matches on the given JSON
     * @param value the JSON string value
     * @return an {@link RequestFieldMatcher} that includes jsonMatch
     */
    public static RequestFieldMatcher equalsToJson(String value) {
        validateJson(value);
        return RequestFieldMatcher.newJsonMatcher(value);
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
        return RequestFieldMatcher.newJsonPathMatch(expression);
    }

    /**
     * Create a matcher that matches on the given XML
     * @param value the XML string value
     * @return an {@link RequestFieldMatcher} that includes xmlMatch
     */
    public static RequestFieldMatcher equalsToXml(String value) {
        validateXml(value);
        return RequestFieldMatcher.newXmlMatcher(value);
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
        return RequestFieldMatcher.newXpathMatcher(expression);
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
