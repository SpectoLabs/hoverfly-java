package io.specto.hoverfly.junit.dsl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.specto.hoverfly.junit.core.ObjectMapperFactory;

/**
 * Interface for converting a java object into a http request body, and storing the appropriate content type header value
 */
public interface HttpBodyConverter {

    ObjectMapper XML_MAPPER = new XmlMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    String APPLICATION_JSON = "application/json";
    String APPLICATION_XML = "application/xml";

    /**
     * Converts a given object into json, and returns application/json content type
     *
     * @param body the body of the request or response
     * @return the converter
     */
    static HttpBodyConverter json(final Object body) {
        return json(body, ObjectMapperFactory.getDefaultObjectMapper());
    }

    /**
     * Reads the input text with possible single quotes as delimiters
     * and returns a String correctly formatted.
     *
     * @param body the body of the request or response
     * @return the converter
     */
    static SingleQuoteHttpBodyConverter jsonWithSingleQuotes(String body) {
        return new SingleQuoteHttpBodyConverter(body);
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
                return marshallJson(body, objectMapper);
            }

            @Override
            public String contentType() {
                return APPLICATION_JSON;
            }
        };
    }

    static HttpBodyConverter xml(final Object body) {
        return xml(body, XML_MAPPER);
    }

    /**
     * Converts a given object into json, and returns application/json content type
     *
     * @param body         the request / response body
     * @param xmlMapper objectMapper to use
     * @return converter
     */
    static HttpBodyConverter xml(final Object body, final ObjectMapper xmlMapper) {
        return new HttpBodyConverter() {
            @Override
            public String body() {
                return marshallJson(body, xmlMapper);
            }

            @Override
            public String contentType() {
                return APPLICATION_XML;
            }
        };
    }

    static String marshallJson(Object body, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot marshall: " + body, e);
        }
    }


    String body();

    String contentType();
}
