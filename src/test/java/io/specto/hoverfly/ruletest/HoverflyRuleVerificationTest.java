package io.specto.hoverfly.ruletest;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.specto.hoverfly.junit.dsl.HttpBodyConverter;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import io.specto.hoverfly.junit.verification.HoverflyVerificationError;
import io.specto.hoverfly.models.SimpleBooking;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.json;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.*;
import static io.specto.hoverfly.junit.verification.HoverflyVerifications.never;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class HoverflyRuleVerificationTest {

    private RestTemplate restTemplate = new RestTemplate();

    private static SimpleBooking booking = new SimpleBooking(1, "London", "Hong Kong", LocalDate.of(2017, 6, 29));

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(dsl(

            service(matches("api*.flight.com"))
                    .get("/api/bookings")
                    .queryParam("airline", contains("Pacific"))
                    .queryParam("page", any())
                    .willReturn(success(json(booking)))

                    .put("/api/bookings/1")
                    .body(equalsToJson(json(booking)))
                    .willReturn(success())

    )).printSimulationData();

    @Before
    public void setUp() {
        hoverflyRule.resetJournal();
    }

    @Test
    public void shouldVerifyRequestHasBeenMadeExactlyOnce() {

        ResponseEntity<SimpleBooking> response = getBookings();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);


        hoverflyRule.verify(service(matches("*.flight.*")).get("/api/bookings").anyQueryParams());

    }

    @Test
    public void shouldVerifyRequestHasNeverBeenMade() {

        ResponseEntity<SimpleBooking> response = getBookings();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        hoverflyRule.verify(service(matches("*.flight.*")).get("/api/bookings").header("Authorization", "Bearer some-token"), never());
    }


    @Test
    public void shouldVerifyRequestWithAJsonBody() throws Exception {
        ResponseEntity<String> bookFlightResponse = putBooking();

        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        hoverflyRule.verify(service("http://api-sandbox.flight.com").put("/api/bookings/1").body(json(booking)));
    }

    @Test
    public void shouldVerifyNeverRequestedForAService() {

        ResponseEntity<SimpleBooking> response = getBookings();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        hoverflyRule.verifyZeroRequestTo(service(matches("api.flight.*")));
    }

    @Test
    public void shouldVerifyAllRequestsHaveBeenMade() throws Exception {

        getBookings();
        putBooking();

        hoverflyRule.verifyAll();
    }

    @Test
    public void shouldThrowExceptionIfVerifyAllFailed() {

       assertThatThrownBy(() -> hoverflyRule.verifyAll())
               .isInstanceOf(HoverflyVerificationError.class)
               .hasMessageContaining("Expected at least 1 request:\n" +
               "{\n" +
               "  \"path\" : {\n" +
               "    \"exactMatch\" : \"/api/bookings/1\"\n" +
               "  },\n" +
               "  \"method\" : {\n" +
               "    \"exactMatch\" : \"PUT\"\n" +
               "  },\n" +
               "  \"destination\" : {\n" +
               "    \"globMatch\" : \"api*.flight.com\"\n" +
               "  },\n" +
               "  \"query\" : {\n" +
               "    \"exactMatch\" : \"\"\n" +
               "  },\n" +
               "  \"body\" : {\n" +
               "    \"jsonMatch\" : \"{\\\"id\\\":1,\\\"origin\\\":\\\"London\\\",\\\"destination\\\":\\\"Hong Kong\\\",\\\"date\\\":\\\"2017-06-29\\\"}\"\n" +
               "  }\n" +
               "}\n" +
               "\n" +
               "But actual number of requests is 0.");
    }

    private ResponseEntity<SimpleBooking> getBookings() {
        URI uri = UriComponentsBuilder.fromHttpUrl("http://api-sandbox.flight.com")
                .path("/api/bookings")
                .queryParam("airline", "Pacific Air")
                .queryParam("page", 1)
                .queryParam("size", 10)
                .build()
                .toUri();

        return restTemplate.getForEntity(uri, SimpleBooking.class);
    }


    private ResponseEntity<String> putBooking() throws URISyntaxException, JsonProcessingException {
        RequestEntity<String> bookFlightRequest = RequestEntity.put(new URI("http://api-sandbox.flight.com/api/bookings/1"))
                .contentType(APPLICATION_JSON)
                .body(HttpBodyConverter.OBJECT_MAPPER.writeValueAsString(booking));

        return restTemplate.exchange(bookFlightRequest, String.class);
    }
}
