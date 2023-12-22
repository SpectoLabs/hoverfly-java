package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import io.specto.hoverfly.junit.core.model.RequestFieldMatcher.MatcherType;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class BodyRequestMatcherCustomDeserializer extends JsonDeserializer<List<RequestFieldMatcher>> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public List<RequestFieldMatcher> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

    CollectionType matcherType = deserializationContext.getTypeFactory().constructCollectionType(List.class, RequestFieldMatcher.class);
    List<RequestFieldMatcher> matchers = deserializationContext.readValue(jsonParser, matcherType);

    matchers.stream()
        .filter(matcher -> matcher.getMatcher() == MatcherType.FORM)
        .forEach(formMatcher -> {
          try {
            String rawFormMatcherValue = objectMapper.writeValueAsString(formMatcher.getValue());
            Map<String, List<RequestFieldMatcher>> formMatcherValue = objectMapper.readValue(
                rawFormMatcherValue, new TypeReference<Map<String, List<RequestFieldMatcher>>>() {});

            formMatcher.setValue(formMatcherValue);
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
        });
    return matchers;
  }
}
