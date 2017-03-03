package io.specto.hoverfly.junit.dsl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Interface for converting a java object into a http request body, and storing the appropriate content type header value
 */
public interface HttpBodyConverter {
    /**
     * Converts a given object into json, and returns application/json content type
     *
     * @param body the body of the request or response
     * @return the converter
     */
    static HttpBodyConverter json(final Object body) {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return json(body, objectMapper);
    }

    /**
     * Converts a given object into json, and returns application/json content type
     *
     * @param body         the request / response body
     * @param objectMapper objectMapper to use
     * @return converter
     */
    static HttpBodyConverter json(final Object body, final ObjectMapper objectMapper) {
        return new HttpBodyConverter() {
            @Override
            public String body() {
                try {
                    return objectMapper.writeValueAsString(body);
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("Cannot marshall: " + body, e);
                }
            }

            @Override
            public String contentType() {
                return "application/json";
            }
        };
    }

    String body();

    String contentType();
}
