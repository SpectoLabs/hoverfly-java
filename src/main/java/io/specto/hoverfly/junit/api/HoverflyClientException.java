package io.specto.hoverfly.junit.api;

public class HoverflyClientException extends RuntimeException {
    private static final long serialVersionUID = 7923725773721731667L;

    public HoverflyClientException(String message) {
        super(message);
    }

    public HoverflyClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
