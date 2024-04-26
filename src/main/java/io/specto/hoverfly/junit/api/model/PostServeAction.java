package io.specto.hoverfly.junit.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostServeAction {

  private final String actionName;
  private final String binary;
  private final String script;
  private final String remote;

  private PostServeAction(
      String actionName,
      String binary,
      String script,
      String remote) {
    this.actionName = actionName;
    this.binary = binary;
    this.script = script;
    this.remote = remote;
  }

  @JsonCreator
  public static PostServeAction newInstance(
      @JsonProperty("actionName") String actionName,
      @JsonProperty("binary") String binary,
      @JsonProperty("script") String script,
      @JsonProperty("remote") String remote) {
    return new PostServeAction(actionName, binary, script, remote);
  }

  public static PostServeAction remote(String actionName, String url) {
    return new PostServeAction(actionName, null, null, url);
  }

  public static PostServeAction local(String actionName, String binary, String script) {
    return new PostServeAction(actionName, binary, script, null);
  }

  public String getActionName() {
    return actionName;
  }

  public String getBinary() {
    return binary;
  }

  public String getScript() {
    return script;
  }

  public String getRemote() {
    return remote;
  }
}
