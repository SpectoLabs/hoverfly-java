package io.specto.hoverfly.junit.verification;

import io.specto.hoverfly.junit.api.HoverflyClient;
import io.specto.hoverfly.junit.core.model.JournalEntry;
import io.specto.hoverfly.junit.dsl.RequestMatcherBuilder;

import java.util.*;
import java.util.stream.Collectors;

import static io.specto.hoverfly.junit.verification.HoverflyVerifications.times;

public class InOrderVerification {


    private HoverflyClient hoverflyClient;
    private List<RequestMatcherBuilder> requestMatchers = new ArrayList<>();

    public InOrderVerification(HoverflyClient hoverflyClient, RequestMatcherBuilder... requestMatchers) {
        this.hoverflyClient = hoverflyClient;
        this.requestMatchers.addAll(Arrays.asList(requestMatchers));
    }

    public void verify() {
        List<JournalEntry> journalEntries = new LinkedList<>();
        requestMatchers.stream()
                .map(RequestMatcherBuilder::build)
                .map(request -> hoverflyClient.searchJournal(request))
                .map(VerificationData::new)
                .map(data -> {
                    times(1).verify(data);
                    JournalEntry entry = data.getJournal().getEntries().iterator().next();
                    journalEntries.add(entry);
                    return entry.getTimeStarted();
                })
                .reduce((last, current) -> {
                    if (current.isBefore(last)) {
                        throw new HoverflyVerificationError(getErrorMessage(journalEntries));
                    } else {
                        return current;
                    }
                });
    }

    private String getErrorMessage(List<JournalEntry> journalEntries) {
        StringBuilder sb = new StringBuilder();
        sb.append("The requests are not in the expected order:").append("\n\n");
        String requests = journalEntries.stream()
                .sorted(Comparator.comparing(JournalEntry::getTimeStarted))
                .map(VerificationUtils::format)
                .collect(Collectors.joining("\n"));
        return sb.append(requests).toString();

    }
}
