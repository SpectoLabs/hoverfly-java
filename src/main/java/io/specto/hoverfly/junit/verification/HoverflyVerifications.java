package io.specto.hoverfly.junit.verification;

import org.apache.commons.lang3.StringUtils;

public class HoverflyVerifications {

    private HoverflyVerifications() {
    }

    public static VerificationCriteria times(int expectedNumberOfRequests) {
        return data -> {
            int actualNumberOfRequests = getActualNumberOfRequests(data);
            if (actualNumberOfRequests != expectedNumberOfRequests) {
                handleVerificationFailure(expectedNumberOfRequests, actualNumberOfRequests, data);
            }
        };
    }

    public static VerificationCriteria never() {
        return times(0);
    }

    public static VerificationCriteria atLeast(int expectedNumberOfRequests) {
        return data -> {
            int actualNumberOfRequests = getActualNumberOfRequests(data);
            if (actualNumberOfRequests < expectedNumberOfRequests) {
                handleVerificationFailure(expectedNumberOfRequests, actualNumberOfRequests, "at least", data);
            }
        };
    }

    public static VerificationCriteria atMost(int expectedNumberOfRequests) {
        return data -> {
            int actualNumberOfRequests = getActualNumberOfRequests(data);
            if (actualNumberOfRequests > expectedNumberOfRequests) {
                handleVerificationFailure(expectedNumberOfRequests, actualNumberOfRequests, "at most", data);
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


    private static void handleVerificationFailure(int expected, int actual, VerificationData data) {
        handleVerificationFailure(expected, actual, "", data);
    }

    private static void handleVerificationFailure(int expected, int actual, String description, VerificationData data) {

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
                sb.append("requests, ");
            } else {
                sb.append("request, ");
            }
        }

        sb.append("but actual number of requests is ").append(actual).append(".");
        sb.append("\n").append("Actual requests found: ").append("\n");

        data.getJournal().getEntries().stream()
                .map(VerificationUtils::format)
                .forEach(formatted -> sb.append(formatted).append("\n"));

        throw new HoverflyVerificationError(sb.toString());
    }


}
