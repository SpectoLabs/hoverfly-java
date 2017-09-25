package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JournalEntry {

    private final RequestDetails request;
    private final Response response;
    private final String mode;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private final LocalDateTime timeStarted;

    private final Integer latency;

    @JsonCreator
    public JournalEntry(@JsonProperty("request") RequestDetails request,
                        @JsonProperty("response") Response response,
                        @JsonProperty("mode") String mode,
                        @JsonProperty("timeStarted") LocalDateTime timeStarted,
                        @JsonProperty("latency") Integer latency) {
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

    public LocalDateTime getTimeStarted() {
        return timeStarted;
    }

    public Integer getLatency() {
        return latency;
    }
}
