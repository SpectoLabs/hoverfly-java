package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestDetails {

    private final String scheme;
    private final String destination;
    private final String path;
    private final String query;
    private final String body;
    private final String method;

    private final Map<String, List<String>> headers = new HashMap<>();

    @JsonCreator
    public RequestDetails(@JsonProperty("scheme") String scheme,
                          @JsonProperty("destination") String destination,
                          @JsonProperty("path") String path,
                          @JsonProperty("query") String query,
                          @JsonProperty("body") String body,
                          @JsonProperty("method") String method,
                          @JsonProperty("headers") Map<String, List<String>> headers) {
        this.scheme = scheme;
        this.destination = destination;
        this.path = path;
        this.query = query;
        this.body = body;
        this.method = method;
        if (headers != null) {
            this.headers.putAll(headers);
        }
    }

    public String getScheme() {
        return scheme;
    }

    public String getDestination() {
        return destination;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }

    public String getBody() {
        return body;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }
}
