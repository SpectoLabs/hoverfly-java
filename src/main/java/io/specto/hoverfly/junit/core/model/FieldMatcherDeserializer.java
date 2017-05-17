package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

class FieldMatcherDeserializer extends JsonDeserializer<FieldMatcher> {
    @Override
    public FieldMatcher deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {

        FieldMatcher matcher = null;
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode jsonNode = codec.readTree(jsonParser);

        if (jsonNode.isObject()) {
            matcher = codec.treeToValue(jsonNode, FieldMatcher.class);
        } else if (jsonNode.isTextual()) {
            matcher = FieldMatcher.fromString(jsonNode.asText());
        }

        return matcher;
    }
}
