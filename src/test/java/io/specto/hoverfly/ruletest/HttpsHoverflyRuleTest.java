package io.specto.hoverfly.ruletest;

import com.google.common.collect.ImmutableMap;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.specto.hoverfly.junit.core.SimulationSource.classpath;
import static java.util.Collections.singletonList;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

public class HttpsHoverflyRuleTest {

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode();

    private RestTemplate restTemplate = new RestTemplate();

    @Test
    public void shouldBeAbleToGetABookingUsingHttps() {

        //Given
        hoverflyRule.simulate(classpath("test-service.json"));

        // When
        final ResponseEntity<String> getBookingResponse = restTemplate.getForEntity("https://www.my-test.com/api/bookings/1", String.class);

        // Then
        assertThat(getBookingResponse.getStatusCode()).isEqualTo(OK);
        assertThatJson(getBookingResponse.getBody()).isEqualTo("{" +
                "\"bookingId\":\"1\"," +
                "\"origin\":\"London\"," +
                "\"destination\":\"Singapore\"," +
                "\"time\":\"2011-09-01T12:30\"," +
                "\"_links\":{\"self\":{\"href\":\"http://localhost/api/bookings/1\"}}" +
                "}");
    }

    @Test
    public void shouldWorkWithRestTemplateWhenHttpsResponseDoesNotContainBodyOrHeaderInTheSimulation() {
        //Given
        hoverflyRule.simulate(classpath("simulations/v5-simulation-without-response-body.json"));

        // When
        final ResponseEntity<String> firstResponse = restTemplate.getForEntity("https://www.my-test.com/api/bookings/1", String.class);
        final ResponseEntity<String> secondResponse = restTemplate.getForEntity("https://www.my-test.com/api/bookings/1", String.class);

        // Then
        assertThat(firstResponse.getStatusCode()).isEqualTo(OK);
        assertThat(firstResponse.getBody()).isNull();
        assertThat(firstResponse.getHeaders()).containsKey("Transfer-Encoding");
        assertThat(secondResponse.getStatusCode()).isEqualTo(OK);
        assertThat(secondResponse.getBody()).isNull();
        assertThat(secondResponse.getHeaders()).containsKey("Transfer-Encoding");
    }
}
