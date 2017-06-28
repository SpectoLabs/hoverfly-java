package io.specto.hoverfly.junit.verification;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.specto.hoverfly.junit.api.HoverflyClient;
import io.specto.hoverfly.junit.core.model.Journal;
import io.specto.hoverfly.junit.core.model.JournalEntry;
import io.specto.hoverfly.junit.core.model.RequestDetails;
import io.specto.hoverfly.junit.dsl.RequestMatcherBuilder;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.time.LocalDateTime;

import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.matches;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InOrderVerificationTest {

    private InOrderVerification inOrder;

    private HoverflyClient hoverflyClient = mock(HoverflyClient.class);


    private ObjectMapper objectMapper = new ObjectMapper();
    private URL resource = Resources.getResource("sample-journal.json");
    private JournalEntry journalEntry;


    @Before
    public void setUp() throws Exception {
        Journal journal = objectMapper.readValue(resource, Journal.class);
        journalEntry = journal.getEntries().iterator().next();
    }

    @Test
    public void shouldDoNothingIfNoRequestMatcherProvided() throws Exception {
        inOrder = new InOrderVerification(hoverflyClient);

        inOrder.verify();
    }

    @Test
    public void shouldVerifyRequestHasBeenMadeExactlyOnce() throws Exception {
        RequestMatcherBuilder rmb = service(matches("api*.com")).get("/v2");
        inOrder = new InOrderVerification(hoverflyClient, rmb);

        when(hoverflyClient.searchJournal(rmb.build())).thenReturn(new Journal(Lists.newArrayList(journalEntry)));

        inOrder.verify();
    }


    @Test
    public void shouldThrowExceptionIfRequestsHasBeenMadeMoreThanOnce() throws Exception {
        RequestMatcherBuilder rmb = service(matches("api*.com")).get("/v2");
        inOrder = new InOrderVerification(hoverflyClient, rmb);

        when(hoverflyClient.searchJournal(rmb.build())).thenReturn(new Journal(Lists.newArrayList(journalEntry, journalEntry)));

        assertThatThrownBy(() -> inOrder.verify())
                .isInstanceOf(HoverflyVerificationError.class).hasMessageContaining("Expected 1 request");
    }

    @Test
    public void shouldThrowExceptionWithDebugInformationIfRequestsAreInTheWrongOrder() throws Exception {
        JournalEntry journalEntry1 = getJournalEntry(LocalDateTime.of(2017, 6, 24, 3, 15, 2), "GET");
        JournalEntry journalEntry2 = getJournalEntry(LocalDateTime.of(2017, 6, 24, 3, 15, 1), "PUT");

        RequestMatcherBuilder rmb1 = service(matches("*hoverfly.*")).get("/v2");
        RequestMatcherBuilder rmb2 = service(matches("*hoverfly.*")).put("/v2");
        inOrder = new InOrderVerification(hoverflyClient, rmb1, rmb2);

        when(hoverflyClient.searchJournal(rmb1.build())).thenReturn(new Journal(Lists.newArrayList(journalEntry1)));
        when(hoverflyClient.searchJournal(rmb2.build())).thenReturn(new Journal(Lists.newArrayList(journalEntry2)));

        assertThatThrownBy(() -> inOrder.verify())
                .isInstanceOf(HoverflyVerificationError.class)
                .hasMessageContaining("are not in the expected order")
                .hasMessageContaining(
                        "[2017-06-24T03:15:01] PUT http://hoverfly.io/ HTTP/1.1\n\n\n" +
                        "[2017-06-24T03:15:02] GET http://hoverfly.io/ HTTP/1.1\n");

    }

    private JournalEntry getJournalEntry(LocalDateTime localDateTime, String method) {
        RequestDetails request = journalEntry.getRequest();
        return new JournalEntry(new RequestDetails(request.getScheme(), request.getDestination(), request.getPath(), request.getQuery(), null, method, null),
                journalEntry.getResponse(), journalEntry.getMode(), localDateTime, journalEntry.getLatency());
    }
}