package io.specto.hoverfly.junit.verification;

import io.specto.hoverfly.junit.core.model.JournalEntry;
import io.specto.hoverfly.junit.core.model.RequestDetails;
import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

class VerificationUtils {

    private VerificationUtils() {
    }

    /**
     * Convert journal entry to request log format
     */
    static String format(JournalEntry entry) {

        RequestDetails request = entry.getRequest();
        String url = formatUrl(request);
        String headers = formatHeaders(request);
        String body = formatBody(request);
        String time = entry.getTimeStarted().format(DateTimeFormatter.ISO_DATE_TIME);

        String requestLog;
        if (StringUtils.isBlank(body)) {
            requestLog = String.format("[%s] %s %s HTTP/1.1\n%s\n", time, request.getMethod().toUpperCase(), url, headers);
        } else {
            requestLog = String.format("[%s] %s %s HTTP/1.1\n%s\n%s\n", time, request.getMethod().toUpperCase(), url, headers, body);
        }
        return requestLog;
    }

    private static String formatBody(RequestDetails request) {
        return request.getBody();
    }

    private static String formatHeaders(RequestDetails request) {
        return request.getHeaders().entrySet().stream()
                .map(entry -> entry.getKey() + ": [" + String.join(", ", entry.getValue()) + "]")
                .collect(Collectors.joining("\n"));
    }

    private static String formatUrl(RequestDetails request) {
        String query = StringUtils.isBlank(request.getQuery()) ? "" : "?" + request.getQuery();
        return request.getScheme() + "://" + request.getDestination() + request.getPath() + query;
    }

}
