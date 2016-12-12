package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Simulation {
    @JsonProperty("data")
    private final HoverflyData hoverflyData;
    @JsonProperty("meta")
    private final HoverflyMetaData hoverflyMetaData;

    @JsonCreator
    public Simulation(@JsonProperty("data") HoverflyData hoverflyData,
                      @JsonProperty("meta") HoverflyMetaData hoverflyMetaData) {
        this.hoverflyData = hoverflyData;
        this.hoverflyMetaData = hoverflyMetaData;
    }

    public HoverflyData getHoverflyData() {
        return hoverflyData;
    }


    public HoverflyMetaData getHoverflyMetaData() {
        return hoverflyMetaData;
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