package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ArrayMatcherConfig.class)
public interface MatcherConfig {
}
