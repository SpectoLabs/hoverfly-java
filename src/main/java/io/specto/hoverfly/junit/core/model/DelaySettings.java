package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class DelaySettings {

    private final String urlPattern;
    private final int delay;


    @JsonCreator
    public DelaySettings(@JsonProperty("urlPattern") String urlPattern,
                         @JsonProperty("delay") int delay) {
        this.urlPattern = urlPattern;
        this.delay = delay;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public int getDelay() {
        return delay;
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
