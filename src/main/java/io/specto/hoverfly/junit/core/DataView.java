package io.specto.hoverfly.junit.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class DataView {
    private final List<RequestResponsePairView> pairs;
    private final GlobalActions globalActions;

    @JsonCreator
    public DataView(@JsonProperty("pairs") List<RequestResponsePairView> pairs,
                    @JsonProperty("globalActions") GlobalActions globalActions) {
        this.pairs = pairs;
        this.globalActions = globalActions;
    }

    public List<RequestResponsePairView> getPairs() {
        return pairs;
    }

    public GlobalActions getGlobalActions() {
        return globalActions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DataView dataView = (DataView) o;

        return new EqualsBuilder()
                .append(pairs, dataView.pairs)
                .append(globalActions, dataView.globalActions)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(pairs)
                .append(globalActions)
                .toHashCode();
    }
}