package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Simulation {
    private final Data data;
    private final MetaData meta;

    @JsonCreator
    public Simulation(@JsonProperty("data") Data data,
                      @JsonProperty("meta") MetaData meta) {
        this.data = data;
        this.meta = meta;
    }

    public Data getData() {
        return data;
    }

    public MetaData getMeta() {
        return meta;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to json string: ", e);
        }
    }


}