package io.specto.hoverfly.junit.verification;

import io.specto.hoverfly.junit.core.model.Request;
import org.apache.commons.lang3.StringUtils;

public class HoverflyVerifications {

    private HoverflyVerifications() {
    }

    public static VerificationCriteria times(int expectedNumberOfRequests) {
        return (request, data) -> {
            int actualNumberOfRequests = getActualNumberOfRequests(data);
            if (actualNumberOfRequests != expectedNumberOfRequests) {
                handleVerificationFailure(expectedNumberOfRequests, actualNumberOfRequests, request, data);
            }
        };
    }

    public static VerificationCriteria never() {
        return times(0);
    }

    public static VerificationCriteria atLeast(int expectedNumberOfRequests) {
        return (request, data) -> {
            int actualNumberOfRequests = getActualNumberOfRequests(data);
            if (actualNumberOfRequests < expectedNumberOfRequests) {
                handleVerificationFailure(expectedNumberOfRequests, actualNumberOfRequests, request, data, "at least");
            }
        };
    }

    public static VerificationCriteria atMost(int expectedNumberOfRequests) {
        return (request, data) -> {
            int actualNumberOfRequests = getActualNumberOfRequests(data);
            if (actualNumberOfRequests > expectedNumberOfRequests) {
                handleVerificationFailure(expectedNumberOfRequests, actualNumberOfRequests, request, data, "at most");
            }
        };
    }

    public static VerificationCriteria atLeastOnce() {
        return atLeast(1);
    }

    private static int getActualNumberOfRequests(VerificationData data) {
        if (data == null || data.getJournal() == null || data.getJournal().getEntries() == null) {
            throw new HoverflyVerificationError("Failed to get journal for verification.");
        }
        return data.getJournal().getEntries().size();
    }


    private static void handleVerificationFailure(int expected, int actual, Request request, VerificationData data) {
        handleVerificationFailure(expected, actual, request, data, "");
    }

    private static void handleVerificationFailure(int expected, int actual, Request request, VerificationData data, String description) {

        StringBuilder sb = new StringBuilder();

        if (expected == 0) {
            sb.append("Not expected any request, ");
        } else {
            sb.append("Expected ");

            if (StringUtils.isNotBlank(description)) {
                sb.append(description).append(" ");
            }

            sb.append(expected).append(" ");

            if (expected > 1) {
                sb.append("requests:").append(System.lineSeparator());
            } else {
                sb.append("request:").append(System.lineSeparator());
            }
        }

        sb.append(request.toString()).append(System.lineSeparator());


        sb.append(System.lineSeparator()).append("But actual number of requests is ").append(actual).append(".");
        if (actual > 0) {
            sb.append("\n").append("Actual requests found: ").append(System.lineSeparator());
        }

        data.getJournal().getEntries().stream()
                .map(VerificationUtils::format)
                .forEach(formatted -> sb.append(formatted).append(System.lineSeparator()));

        throw new HoverflyVerificationError(sb.toString());
    }


}
