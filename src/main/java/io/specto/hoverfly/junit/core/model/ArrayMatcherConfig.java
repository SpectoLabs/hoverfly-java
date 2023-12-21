package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArrayMatcherConfig implements MatcherConfig {

    private boolean ignoreUnknown;
    private boolean ignoreOrder;
    private boolean ignoreOccurrences;

    public ArrayMatcherConfig() {
    }

    public ArrayMatcherConfig(boolean ignoreUnknown, boolean ignoreOrder, boolean ignoreOccurrences) {
        this.ignoreUnknown = ignoreUnknown;
        this.ignoreOrder = ignoreOrder;
        this.ignoreOccurrences = ignoreOccurrences;
    }

    public boolean isIgnoreUnknown() {
        return ignoreUnknown;
    }

    public void setIgnoreUnknown(boolean ignoreUnknown) {
        this.ignoreUnknown = ignoreUnknown;
    }

    public boolean isIgnoreOrder() {
        return ignoreOrder;
    }

    public void setIgnoreOrder(boolean ignoreOrder) {
        this.ignoreOrder = ignoreOrder;
    }

    public boolean isIgnoreOccurrences() {
        return ignoreOccurrences;
    }

    public void setIgnoreOccurrences(boolean ignoreOccurrences) {
        this.ignoreOccurrences = ignoreOccurrences;
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
