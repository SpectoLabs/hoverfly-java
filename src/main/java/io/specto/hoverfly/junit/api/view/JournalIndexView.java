package io.specto.hoverfly.junit.api.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JournalIndexView {

  private final String name;
  private final List<JournalIndexEntry> entries;

  @JsonCreator
  public JournalIndexView(
      @JsonProperty("name") String name,
      @JsonProperty("entries") List<JournalIndexEntry> entries) {
    this.name = name;
    this.entries = entries;
  }

  public String getName() {
    return name;
  }

  public List<JournalIndexEntry> getEntries() {
    return entries;
  }

  public static class JournalIndexEntry {

    private final String key;
    private final String journalEntryId;

    @JsonCreator
    private JournalIndexEntry(@JsonProperty("key") String key, @JsonProperty("journalEntryId") String journalEntryId) {
      this.key = key;
      this.journalEntryId = journalEntryId;
    }

    public String getKey() {
      return key;
    }

    public String getJournalEntryId() {
      return journalEntryId;
    }
  }
}
