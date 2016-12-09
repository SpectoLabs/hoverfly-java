package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class GlobalActions {

    private final List<DelaySettings> delays;

    @JsonCreator
    public GlobalActions(@JsonProperty("delays") List<DelaySettings> delays) {
        this.delays = delays;
    }

    public List<DelaySettings> getDelays() {
        return delays;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
