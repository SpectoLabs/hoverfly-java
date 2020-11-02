package io.specto.hoverfly.junit.verification;

public class HoverflyVerificationError extends AssertionError {

    public HoverflyVerificationError(String message) {
        super(message);
    }
}
