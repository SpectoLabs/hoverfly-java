package io.specto.hoverfly.junit.api.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DiffReportEntry {

    private final String field;
    private final String expected;
    private final String actual;

    @JsonCreator
    public DiffReportEntry(
        @JsonProperty("field") String field,
        @JsonProperty("expected") String expected,
        @JsonProperty("actual") String actual){
        this.field = field;
        this.expected = expected;
        this.actual = actual;
    }

    public String getField() {
        return field;
    }

    public String getExpected() {
        return expected;
    }

    public String getActual() {
        return actual;
    }
}
