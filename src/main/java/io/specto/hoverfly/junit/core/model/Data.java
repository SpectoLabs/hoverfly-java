package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Set;

public class Data {
    private final Set<RequestResponsePair> pairs;
    private final GlobalActions globalActions;

    @JsonCreator
    public Data(@JsonProperty("pairs") Set<RequestResponsePair> pairs,
                @JsonProperty("globalActions") GlobalActions globalActions) {
        this.pairs = pairs;
        this.globalActions = globalActions;
    }

    public Set<RequestResponsePair> getPairs() {
        return pairs;
    }

    public GlobalActions getGlobalActions() {
        return globalActions;
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