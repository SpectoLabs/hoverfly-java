package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LogNormalDelay {

    private final int min;
    private final int max;
    private final int mean;
    private final int median;

    @JsonCreator
    public LogNormalDelay(
            @JsonProperty("min") int min,
            @JsonProperty("max") int max,
            @JsonProperty("mean") int mean,
            @JsonProperty("median") int median) {
        this.min = min;
        this.max = max;
        this.mean = mean;
        this.median = median;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getMean() {
        return mean;
    }

    public int getMedian() {
        return median;
    }
}
