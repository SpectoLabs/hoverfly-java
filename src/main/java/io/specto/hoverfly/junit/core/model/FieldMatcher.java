package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldMatcher {

    private final String exactMatch;
    private final String globMatch;
    private final String regexMatch;
    private final String jsonMatch;
    private final String jsonPathMatch;
    private final String xmlMatch;
    private final String xpathMatch;


    @JsonCreator
    public FieldMatcher(@JsonProperty("exactMatch") String exactMatch,
                        @JsonProperty("globMatch") String globMatch,
                        @JsonProperty("regexMatch") String regexMatch,
                        @JsonProperty("jsonMatch") String jsonMatch,
                        @JsonProperty("jsonPathMatch") String jsonPathMatch,
                        @JsonProperty("xmlMatch") String xmlMatch,
                        @JsonProperty("xpathMatch") String xpathMatch) {
        this.exactMatch = exactMatch;
        this.globMatch = globMatch;
        this.jsonMatch = jsonMatch;
        this.regexMatch = regexMatch;
        this.jsonPathMatch = jsonPathMatch;
        this.xmlMatch = xmlMatch;
        this.xpathMatch = xpathMatch;
    }

    public static FieldMatcher exactlyMatches(String value) {
        return new Builder().exactMatch(value).build();
    }

    public static FieldMatcher wildCardMatches(String pattern) {
        return new Builder().globMatch(pattern).build();
    }


    public String getExactMatch() {
        return exactMatch;
    }

    public String getGlobMatch() {
        return globMatch;
    }

    public String getJsonMatch() {
        return jsonMatch;
    }

    public String getRegexMatch() {
        return regexMatch;
    }

    public String getXpathMatch() {
        return xpathMatch;
    }

    public String getJsonPathMatch() {
        return jsonPathMatch;
    }

    public String getXmlMatch() {
        return xmlMatch;
    }

    static FieldMatcher fromString(String value) {
        Builder builder = new Builder();
        if (value.contains("*")) {
            builder.globMatch(value);
        } else {
            builder.exactMatch(value);
        }
        return builder.build();
    }

    public static class Builder {
        private String exactMatch = null;
        private String globMatch = null;
        private String regexMatch = null;
        private String jsonMatch = null;
        private String jsonPathMatch = null;
        private String xmlMatch = null;
        private String xpathMatch = null;

        public Builder exactMatch(String exactMatch) {
            this.exactMatch = exactMatch;
            return this;
        }

        public Builder globMatch(String globMatch) {
            this.globMatch = globMatch;
            return this;
        }

        public Builder regexMatch(String regexMatch) {
            this.regexMatch = regexMatch;
            return this;
        }

        public Builder jsonMatch(String jsonMatch) {
            this.jsonMatch = jsonMatch;
            return this;
        }

        public Builder jsonPathMatch(String jsonPathMatch) {
            this.jsonPathMatch = jsonPathMatch;
            return this;
        }

        public Builder xmlMatch(String xmlMatch) {
            this.xmlMatch = xmlMatch;
            return this;
        }

        public Builder xpathMatch(String xpathMatch) {
            this.xpathMatch = xpathMatch;
            return this;
        }

        public FieldMatcher build() {
            return new FieldMatcher(exactMatch, globMatch, regexMatch, jsonMatch, jsonPathMatch, xmlMatch, xpathMatch);
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
