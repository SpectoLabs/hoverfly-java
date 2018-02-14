package io.specto.hoverfly.junit.api.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleRequestDefinitionView {

    private final String method;
    private final String host;
    private final String path;
    private final String query;

    @JsonCreator
    public SimpleRequestDefinitionView(
        @JsonProperty("method") String method,
        @JsonProperty("host") String host,
        @JsonProperty("path") String path,
        @JsonProperty("query") String query) {
        this.method = method;
        this.host = host;
        this.path = path;
        this.query = query;
    }

    public String getMethod() {
        return method;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return "[" +
            "method='" + method + '\'' +
            ", host='" + host + '\'' +
            ", path='" + path + '\'' +
            ", query='" + query + '\'' +
            ']';
    }
}
