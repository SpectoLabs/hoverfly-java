package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.Map;

import static io.specto.hoverfly.junit.core.model.RequestFieldMatcher.MatcherType.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestFieldMatcher<T> {

    private MatcherType matcher;
    private T value;
    private MatcherConfig config;

    public RequestFieldMatcher() {
    }

    public RequestFieldMatcher(MatcherType matcher, T value) {
        this.matcher = matcher;
        this.value = value;
    }

    public RequestFieldMatcher(MatcherType matcher, T value, MatcherConfig config) {
        this.matcher = matcher;
        this.value = value;
        this.config = config;
    }

    public MatcherType getMatcher() {
        return matcher;
    }

    public void setMatcher(MatcherType matcher) {
        this.matcher = matcher;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public MatcherConfig getConfig() {
        return config;
    }

    public void setConfig(MatcherConfig config) {
        this.config = config;
    }

    public static RequestFieldMatcher<String> newExactMatcher(String value) {
        return new RequestFieldMatcher<>(EXACT, value);
    }

    public static RequestFieldMatcher<String> newGlobMatcher(String value) {
        return new RequestFieldMatcher<>(MatcherType.GLOB, value);
    }

    public static RequestFieldMatcher<String> newRegexMatcher(String value) {
        return new RequestFieldMatcher<>(REGEX, value);
    }

    public static RequestFieldMatcher<List<String>> newArrayMatcher(List<String> value) {
        return new RequestFieldMatcher<>(ARRAY, value);
    }

    public static RequestFieldMatcher<List<String>> newArrayMatcher(List<String> value, ArrayMatcherConfig arrayMatcherConfig) {
        return new RequestFieldMatcher<>(ARRAY, value, arrayMatcherConfig);
    }

    public static RequestFieldMatcher<Map<String, List<RequestFieldMatcher<?>>>> newFormMatcher(Map<String, List<RequestFieldMatcher<?>>> value) {
        return new RequestFieldMatcher<>(FORM, value);
    }

    public static RequestFieldMatcher<String> newJwtMatcher(String value) {
        return new RequestFieldMatcher<>(JWT, value);
    }

    public static RequestFieldMatcher<String> newXmlMatcher(String value) {
        return new RequestFieldMatcher<>(MatcherType.XML, value);
    }

    public static RequestFieldMatcher<String> newXpathMatcher(String value) {
        return new RequestFieldMatcher<>(MatcherType.XPATH, value);
    }

    public static RequestFieldMatcher<String> newJsonMatcher(String value) {
        return new RequestFieldMatcher<>(MatcherType.JSON, value);
    }


    public static RequestFieldMatcher<String> newJsonPartialMatcher(String value) {
        return new RequestFieldMatcher<>(MatcherType.JSONPARTIAL, value);
    }

    public static RequestFieldMatcher<String> newJsonPathMatch(String value) {
        return new RequestFieldMatcher<>(MatcherType.JSONPATH, value);
    }


    public enum MatcherType {
        EXACT,
        GLOB,
        REGEX,
        ARRAY,
        JWT,
        FORM,
        XML,
        XPATH,
        JSON,
        JSONPARTIAL,
        JSONPATH;

        @JsonCreator
        public static MatcherType fromValue(String value) {
            return value == null ? null : MatcherType.valueOf(value.toUpperCase());
        }

        @JsonValue
        public String getValue() {
            return name().toLowerCase();
        }
    }


    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
