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
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        GlobalActions that = (GlobalActions) o;

        return new EqualsBuilder()
                .append(delays, that.delays)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(delays)
                .toHashCode();
    }
}
