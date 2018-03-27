package io.specto.hoverfly.junit.api.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.IntStream;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseDiffForRequestView {
    private final SimpleRequestDefinitionView request;
    private final List<DiffReport> diffReports;

    @JsonCreator
    public ResponseDiffForRequestView(
        @JsonProperty("request") SimpleRequestDefinitionView request,
        @JsonProperty("diffReports") List<DiffReport> diffReports) {
        this.request = request;
        this.diffReports = diffReports;
    }

    public String createDiffMessage() {
        StringBuilder message = new StringBuilder()
            .append("\nFor the request with the simple definition:\n" + getRequest().toString())
            .append("\n\nhave been recorded " + getDiffReports().size() + " diff(s):\n");

        IntStream.range(0, getDiffReports().size())
            .forEach(index -> {
                DiffReport diffReport = getDiffReports().get(index);
                message
                    .append(String.format("\n%s. diff report at %s:\n", (index + 1), diffReport.getTimestamp()))
                    .append(diffReport.createDiffMessage() + "\n");
            });
        return message.toString();
    }

    public SimpleRequestDefinitionView getRequest() {
        return request;
    }

    public List<DiffReport> getDiffReports() {
        return diffReports;
    }
}
