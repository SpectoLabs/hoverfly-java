package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static io.specto.hoverfly.junit.core.SimulationSource.classpath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Ignore
public class HoverflyRuleRemoteInstanceTest {

    /**
     * Pre-requisite for running the tests:
     * 1. A working Remote Hoverfly instance with security enabled
     * 2. Export auth token as env variable HOVERFLY_AUTH_TOKEN
     * 3. CA cert for the remote Hoverfly instance
     */

    private static final String REMOTE_HOST = "solid-terminal-tommysitu.hoverfly.io";

    private RestTemplate restTemplate = new RestTemplate();

    @Rule
    public EnvironmentVariables envVars = new EnvironmentVariables();

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(classpath("test-service-https.json"),
            configs()
                    .remote()
                    .host(REMOTE_HOST)
                    .withHttpsAdminEndpoint()
                    .withAuthHeader()
                    .proxyCaCert("hfc-ca-signed.pem"));

    @Test
    public void shouldBeAbleToMakeABookingUsingHoverfly() throws URISyntaxException {
        // Given
        final RequestEntity<String> bookFlightRequest = RequestEntity.post(new URI("https://www.my-test.com/api/bookings"))
                .contentType(APPLICATION_JSON)
                .header(hoverflyRule.getAuthHeaderName(), hoverflyRule.getAuthHeaderValue())
                .body("{\"flightId\": \"1\"}");

        // When
        final ResponseEntity<String> bookFlightResponse = restTemplate.exchange(bookFlightRequest, String.class);

        // Then
        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(CREATED);
        assertThat(bookFlightResponse.getHeaders().getLocation()).isEqualTo(new URI("https://www.my-test.com/api/bookings/1"));
    }
}
