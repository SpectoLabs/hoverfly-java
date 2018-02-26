package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Journal {


    @JsonProperty("journal")
    private final List<JournalEntry> entries;

    private final int offset;
    private final int limit;
    private final int total;


    @JsonCreator
    public Journal(@JsonProperty("journal") List<JournalEntry> entries,
                   @JsonProperty("offset") int offset,
                   @JsonProperty("limit") int limit,
                   @JsonProperty("total") int total) {
        this.entries = entries;
        this.offset = offset;
        this.limit = limit;
        this.total = total;
    }

    public List<JournalEntry> getEntries() {
        return entries;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public int getTotal() {
        return total;
    }
}
