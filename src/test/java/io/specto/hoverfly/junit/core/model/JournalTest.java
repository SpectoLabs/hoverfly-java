package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

public class JournalTest {


    private ObjectMapper objectMapper = new ObjectMapper();
    private URL resource = Resources.getResource("sample-journal.json");

    @Before
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void shouldDeserializeFromJson() throws Exception {

        Journal journal = objectMapper.readValue(resource, Journal.class);


        assertThat(journal.getEntries()).hasSize(1);

        JournalEntry logEntry = journal.getEntries().iterator().next();

        assertThat(logEntry.getMode()).isEqualTo("simulate");
        RequestDetails request = logEntry.getRequest();
        assertThat(request.getDestination()).isEqualTo("hoverfly.io");
        assertThat(request.getScheme()).isEqualTo("http");
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/");
        assertThat(request.getHeaders()).containsAllEntriesOf(ImmutableMap.of(
                "Accept", Lists.newArrayList("*/*"),
                "User-Agent", Lists.newArrayList("curl/7.49.1")));

        assertThat(logEntry.getResponse()).isNotNull();
        assertThat(logEntry.getLatency()).isEqualTo(2);
        assertThat(logEntry.getTimeStarted()).isEqualTo(
                ZonedDateTime.parse("2017-06-22T13:18:08.050+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

    }
}