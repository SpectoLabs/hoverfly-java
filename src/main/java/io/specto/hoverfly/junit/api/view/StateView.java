package io.specto.hoverfly.junit.api.view;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StateView {
    private final Map<String, String> state;

    @JsonCreator
    public StateView(@JsonProperty("state") Map<String, String> state) {
        this.state = state;
    }

    public Map<String, String> getState() {
        return state;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final StateView stateView = (StateView) o;
        return Objects.equals(state, stateView.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state);
    }
}
