package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static io.specto.hoverfly.junit.core.HoverflyConfig.authenticationConfigs;
import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static io.specto.hoverfly.junit.core.SimulationSource.classpath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Ignore
public class HoverflyRuleRemoteInstanceTest {

    // Use a working remote hoverfly instance host
    private static final String REMOTE_HOST = "junior-tennis-tommysitu.hoverfly.io";

    private RestTemplate restTemplate = new RestTemplate();

    private static final String authToken = "token";
    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(classpath("test-service-https.json"),
            configs()
                    .useRemoteInstance(REMOTE_HOST,
                        authenticationConfigs()
                                .withHttps("hfc-self-signed.pem")
                                .withAuthHeader(authToken)));

    @Test
    public void shouldBeAbleToMakeABookingUsingHoverfly() throws URISyntaxException {
        // Given
        final RequestEntity<String> bookFlightRequest = RequestEntity.post(new URI("https://www.my-test.com/api/bookings"))
                .contentType(APPLICATION_JSON)
                .header("Proxy-Authorization", "Bearer " + authToken)
                .body("{\"flightId\": \"1\"}");

        // When
        final ResponseEntity<String> bookFlightResponse = restTemplate.exchange(bookFlightRequest, String.class);

        // Then
        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(CREATED);
        assertThat(bookFlightResponse.getHeaders().getLocation()).isEqualTo(new URI("https://www.my-test.com/api/bookings/1"));
    }
}
