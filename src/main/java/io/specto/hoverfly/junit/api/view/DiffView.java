package io.specto.hoverfly.junit.api.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DiffView {

    private final List<ResponseDiffForRequestView> diffs;

    @JsonCreator
    public DiffView(@JsonProperty("diff") List<ResponseDiffForRequestView> diffs) {
        this.diffs = diffs;
    }

    public List<ResponseDiffForRequestView> getDiffs() {
        return diffs;
    }
}
