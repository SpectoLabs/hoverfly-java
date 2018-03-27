package io.specto.hoverfly.junit.api.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DiffReport {

    private final String timestamp;
    private final List<DiffReportEntry> entries;

    @JsonCreator
    public DiffReport(
        @JsonProperty("timestamp") String timestamp,
        @JsonProperty("diffEntries") List<DiffReportEntry> entries){
        this.timestamp = timestamp;
        this.entries = entries;
    }

    public String createDiffMessage() {
        String msgTemplate = "(%s.) The \"%s\" parameter is not same - the expected value was [%s], but the actual one [%s]\n";
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= entries.size(); i++){
            DiffReportEntry entry = entries.get(i - 1);
            sb.append(String.format(msgTemplate, i, entry.getField(), entry.getExpected(), entry.getActual()));
        }
        return sb.toString();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public List<DiffReportEntry> getEntries() {
        return entries;
    }
}
