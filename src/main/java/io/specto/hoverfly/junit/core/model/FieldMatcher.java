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
    private final String jsonMatch;
    private final String regexMatch;
    private final String xpathMatch;


    @JsonCreator
    public FieldMatcher(@JsonProperty("exactMatch") String exactMatch,
                        @JsonProperty("globMatch") String globMatch,
                        @JsonProperty("jsonMatch") String jsonMatch,
                        @JsonProperty("regexMatch") String regexMatch,
                        @JsonProperty("xpathMatch") String xpathMatch) {
        this.exactMatch = exactMatch;
        this.globMatch = globMatch;
        this.jsonMatch = jsonMatch;
        this.regexMatch = regexMatch;
        this.xpathMatch = xpathMatch;
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


    public static FieldMatcher fromExactMatchString(String value) {
        return new Builder().exactMatch(value).build();
    }

    static class Builder {
        private String exactMatch = null;
        private String globMatch = null;
        private String jsonMatch = null;
        private String regexMatch = null;
        private String xpathMatch = null;

        Builder exactMatch(String exactMatch) {
            this.exactMatch = exactMatch;
            return this;
        }

        Builder globMatch(String globMatch) {
            this.globMatch = globMatch;
            return this;
        }

        Builder jsonMatch(String jsonMatch) {
            this.jsonMatch = jsonMatch;
            return this;
        }

        Builder regexMatch(String regexMatch) {
            this.regexMatch = regexMatch;
            return this;
        }

        Builder xpathMatch(String xpathMatch) {
            this.xpathMatch = xpathMatch;
            return this;
        }

        FieldMatcher build() {
            return new FieldMatcher(exactMatch, globMatch, jsonMatch, regexMatch, xpathMatch);
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
