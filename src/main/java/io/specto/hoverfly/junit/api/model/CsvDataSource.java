package io.specto.hoverfly.junit.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CsvDataSource {

  private final String name;
  private final String data;

  @JsonCreator
  public CsvDataSource(@JsonProperty("name") String name, @JsonProperty("data") String data) {
    this.name = name;
    this.data = data;
  }

  public String getName() {
    return name;
  }

  public String getData() {
    return data;
  }
}
