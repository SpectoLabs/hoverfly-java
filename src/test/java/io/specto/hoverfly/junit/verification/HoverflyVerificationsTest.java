package io.specto.hoverfly.junit.verification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.specto.hoverfly.junit.core.model.Journal;
import io.specto.hoverfly.junit.core.model.JournalEntry;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HoverflyVerificationsTest {

    private ObjectMapper objectMapper = new ObjectMapper();
    private URL resource = Resources.getResource("sample-journal.json");
    private JournalEntry journalEntry;


    @Before
    public void setUp() throws Exception {
        Journal journal = objectMapper.readValue(resource, Journal.class);
        journalEntry = journal.getEntries().iterator().next();
    }

    @Test
    public void shouldVerifyByNumberOfTimes() throws Exception {
        VerificationData data = new VerificationData(new Journal(Lists.newArrayList(journalEntry)));
        HoverflyVerifications.times(1).verify(data);
    }

    @Test
    public void shouldThrowExceptionWhenVerifyWithTimesFailed() throws Exception {
        VerificationData data = new VerificationData(new Journal(Collections.emptyList()));
        assertThatThrownBy(() -> HoverflyVerifications.times(1).verify(data))
                .isInstanceOf(HoverflyVerificationError.class)
                .hasMessageContaining("Expected 1 request, but actual number of requests is 0");
    }


    @Test
    public void shouldThrowHoverflyVerificationExceptionIfJournalIsNull() throws Exception {
        VerificationData data = new VerificationData();
        assertThatThrownBy(() -> HoverflyVerifications.times(1).verify(data))
                .isInstanceOf(HoverflyVerificationError.class)
                .hasMessageContaining("Failed to get journal");
    }


    @Test
    public void shouldVerifyRequestNeverMade() throws Exception {
        VerificationData data = new VerificationData(new Journal(Collections.emptyList()));
        HoverflyVerifications.never().verify(data);
    }

    @Test
    public void shouldThrowExceptionIfVerifyWithNeverFailed() throws Exception {
        VerificationData data = new VerificationData(new Journal(Lists.newArrayList(journalEntry)));
        assertThatThrownBy(() -> HoverflyVerifications.never().verify(data))
                .isInstanceOf(HoverflyVerificationError.class)
                .hasMessageContaining("Not expected any request, but actual number of requests is 1");
    }


    @Test
    public void shouldVerifyWithAtLeastNumberOfTimes() throws Exception {
        VerificationData data = new VerificationData(new Journal(Lists.newArrayList(journalEntry, journalEntry, journalEntry)));

        HoverflyVerifications.atLeast(3).verify(data);
        HoverflyVerifications.atLeast(2).verify(data);
        HoverflyVerifications.atLeast(1).verify(data);
    }


    @Test
    public void shouldThrowExceptionWhenVerifyWithAtLeastTwoTimesButOnlyOneRequestWasMade() throws Exception {

        VerificationData data = new VerificationData(new Journal(Lists.newArrayList(journalEntry)));
        assertThatThrownBy(() -> HoverflyVerifications.atLeast(2).verify(data))
                .isInstanceOf(HoverflyVerificationError.class)
                .hasMessageContaining("Expected at least 2 requests, but actual number of requests is 1");
    }

    @Test
    public void shouldVerifyWithAtLeastOnce() throws Exception {
        VerificationData data = new VerificationData(new Journal(Lists.newArrayList(journalEntry, journalEntry, journalEntry)));

        HoverflyVerifications.atLeastOnce().verify(data);
    }

    @Test
    public void shouldVerifyWithAtMostNumberOfTimes() throws Exception {
        VerificationData data = new VerificationData(new Journal(Lists.newArrayList(journalEntry, journalEntry, journalEntry)));

        HoverflyVerifications.atMost(3).verify(data);
    }


    @Test
    public void shouldVerifyWithAmostThreeRequestsButOnlyTwoRequestsWereMade() throws Exception {
        VerificationData data = new VerificationData(new Journal(Lists.newArrayList(journalEntry, journalEntry)));

        HoverflyVerifications.atMost(3).verify(data);
    }


    @Test
    public void shouldThrowExceptionWhenVerifyWithAtMostTwoTimesButThreeRequestsWereMade() throws Exception {
        VerificationData data = new VerificationData(new Journal(Lists.newArrayList(journalEntry, journalEntry, journalEntry)));
        assertThatThrownBy(() -> HoverflyVerifications.atMost(2).verify(data))
                .isInstanceOf(HoverflyVerificationError.class)
                .hasMessageContaining("Expected at most 2 requests, but actual number of requests is 3");
    }
}
