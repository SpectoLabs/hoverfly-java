package io.specto.hoverfly.junit.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ObjectMapperFactory {

  private static final ObjectWriter JSON_PRETTY_PRINTER = new ObjectMapper().writerWithDefaultPrettyPrinter();
  private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

  public static ObjectWriter getPrettyPrinter() {
    return JSON_PRETTY_PRINTER;
  }

  public static ObjectMapper getDefaultObjectMapper() {
    return DEFAULT_OBJECT_MAPPER;
  }
}
