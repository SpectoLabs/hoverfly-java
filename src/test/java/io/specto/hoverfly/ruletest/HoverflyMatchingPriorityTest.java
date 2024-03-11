package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit.dsl.HoverflyDsl;
import io.specto.hoverfly.junit.dsl.HttpBodyConverter;
import io.specto.hoverfly.junit.dsl.ResponseCreators;
import io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.assertj.core.api.Assertions;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class HoverflyMatchingPriorityTest {

  @ClassRule
  public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode();

  @Test
  public void shouldPassWhenSpecificBodyMatcherDeclaredFirst() {
    hoverflyRule.simulate(SimulationSource.dsl(
        HoverflyDsl.service("https://test.com")
            .post("/sameUrl")
            .body(HoverflyMatchers.matchesPartialJson(HttpBodyConverter.jsonWithSingleQuotes("{'attr':'value'}")))
            .willReturn(ResponseCreators.success())
            .post("/sameUrl")
            .anyBody()
            .willReturn(ResponseCreators.badRequest())
    ));
    Assertions.assertThat(new RestTemplate().exchange("https://test.com/sameUrl", HttpMethod.POST, new HttpEntity<>(" {\n"
            + "              \"test\": 1,\n"
            + "              \"attr\": \"value\",\n"
            + "              \"list\": [ \"A\", \"B\" ]\n"
            + "            }"), String.class))
        .isNotNull();
  }

  @Test
  public void shouldPassWhenSpecificBodyMatcherDeclaredLast() {
    hoverflyRule.simulate(SimulationSource.dsl(
        HoverflyDsl.service("https://test.com")
            .post("/sameUrl")
            .anyBody()
            .willReturn(ResponseCreators.badRequest())
            .post("/sameUrl")
            .body(HoverflyMatchers.matchesPartialJson(HttpBodyConverter.jsonWithSingleQuotes("{'attr':'value'}")))
            .willReturn(ResponseCreators.success())

    ));
    Assertions.assertThat(new RestTemplate().exchange("https://test.com/sameUrl", HttpMethod.POST, new HttpEntity<>(" {\n"
            + "              \"test\": 1,\n"
            + "              \"attr\": \"value\",\n"
            + "              \"list\": [ \"A\", \"B\" ]\n"
            + "            }"), String.class))
        .isNotNull();
  }

}
