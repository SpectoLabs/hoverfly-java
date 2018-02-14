package io.specto.hoverfly.junit.api.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseDiffForRequestView {
    private final SimpleRequestDefinitionView request;
    private final List<String> diffMessages;

    @JsonCreator
    public ResponseDiffForRequestView(
        @JsonProperty("request") SimpleRequestDefinitionView request,
        @JsonProperty("diffMessage") List<String> diffMessages) {
        this.request = request;
        this.diffMessages = diffMessages;
    }

    public SimpleRequestDefinitionView getRequest() {
        return request;
    }

    public List<String> getDiffMessages() {
        return diffMessages;
    }
}
