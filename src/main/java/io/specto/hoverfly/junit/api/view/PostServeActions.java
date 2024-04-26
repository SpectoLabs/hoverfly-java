package io.specto.hoverfly.junit.api.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.specto.hoverfly.junit.api.model.PostServeAction;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PostServeActions {

  private final List<PostServeAction> actions;

  @JsonCreator
  public PostServeActions(@JsonProperty("actions") List<PostServeAction> actions) {
    this.actions = actions == null ? Collections.emptyList() : actions;
  }

  public List<PostServeAction> getActions() {
    return actions;
  }
}
