package io.specto.hoverfly.junit.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class DelaySetting {

    private final String urlPattern;
    private final int delay;


    @JsonCreator
    public DelaySetting(@JsonProperty("urlPattern") String urlPattern,
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
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DelaySetting that = (DelaySetting) o;

        return new EqualsBuilder()
                .append(delay, that.delay)
                .append(urlPattern, that.urlPattern)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(urlPattern)
                .append(delay)
                .toHashCode();
    }
}
