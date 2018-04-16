package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JournalEntry {

    private final RequestDetails request;
    private final Response response;
    private final String mode;

    private final ZonedDateTime timeStarted;

    private final Double latency;

    @JsonCreator
    public JournalEntry(@JsonProperty("request") RequestDetails request,
                        @JsonProperty("response") Response response,
                        @JsonProperty("mode") String mode,
                        @JsonProperty("timeStarted") ZonedDateTime timeStarted,
                        @JsonProperty("latency") Double latency) {
        this.request = request;
        this.response = response;
        this.mode = mode;
        this.timeStarted = timeStarted;
        this.latency = latency;
    }


    public RequestDetails getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }

    public String getMode() {
        return mode;
    }

    public ZonedDateTime getTimeStarted() {
        return timeStarted;
    }

    public Double getLatency() {
        return latency;
    }
}
