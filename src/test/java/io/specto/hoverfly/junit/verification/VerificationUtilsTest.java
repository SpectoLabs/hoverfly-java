package io.specto.hoverfly.junit.verification;

import com.google.common.collect.ImmutableMap;
import io.specto.hoverfly.junit.core.model.JournalEntry;
import io.specto.hoverfly.junit.core.model.RequestDetails;
import org.assertj.core.util.Lists;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;


public class VerificationUtilsTest {

    @Test
    public void shouldFormatJournalEntryToRequestLogFormat() {


        RequestDetails request = new RequestDetails("http", "localhost:8080", "/api/v1", "id=123", "{\"id\":123}", "PUT", ImmutableMap.of("Authorization", Lists.newArrayList("Bearer some-token")));
        JournalEntry journalEntry = new JournalEntry(request, null, "simulate", ZonedDateTime.of(2017, 6, 24, 3, 15, 1, 0, ZoneId.of("UTC")), 2d);
        String requestLog = VerificationUtils.format(journalEntry);

        assertThat(requestLog).isEqualTo("[2017-06-24T03:15:01] PUT http://localhost:8080/api/v1?id=123 HTTP/1.1\n" +
                "Authorization: [Bearer some-token]\n" +
                "{\"id\":123}\n");
    }
}