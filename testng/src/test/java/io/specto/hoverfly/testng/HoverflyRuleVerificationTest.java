package io.specto.hoverfly.testng;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.json;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.any;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.contains;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.equalsToJson;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.matches;
import static io.specto.hoverfly.junit.verification.HoverflyVerifications.never;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.specto.hoverfly.junit.core.ObjectMapperFactory;
import io.specto.hoverfly.junit.verification.HoverflyVerificationError;
import io.specto.hoverfly.models.SimpleBooking;
import io.specto.hoverfly.testng.api.TestNGClassRule;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(HoverflyExecutor.class)
public class HoverflyRuleVerificationTest {

    private static final String NL = System.lineSeparator();
    private static final SimpleBooking BOOKING = new SimpleBooking(1, "London", "Hong Kong", LocalDate.of(2017, 6, 29));
    private final RestTemplate restTemplate = new RestTemplate();


    @TestNGClassRule
    public static HoverflyTestNG hoverflyRule = HoverflyTestNG.inSimulationMode(dsl(

            service(matches("api*.flight.com"))
                    .get("/api/bookings")
                    .queryParam("airline", contains("Pacific"))
                    .queryParam("page", any())
                    .willReturn(success(json(BOOKING)))

                    .put("/api/bookings/1")
                    .body(equalsToJson(json(BOOKING)))
                    .willReturn(success())

    )).printSimulationData();

    @BeforeMethod
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

        hoverflyRule.verify(service(matches("*.flight.*")).get("/api/bookings").header("Authorization", matches("Bearer *")), never());
    }


    @Test
    public void shouldVerifyRequestWithAJsonBody() throws Exception {
        ResponseEntity<String> bookFlightResponse = putBooking();

        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        hoverflyRule.verify(service("http://api-sandbox.flight.com").put("/api/bookings/1").header("Content-Type", any()).body(json(
            BOOKING)));
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
               .hasMessageContaining("Expected at least 1 request:" + NL +
                       "{" + NL +
                       "  \"path\" : [ {" + NL +
                       "    \"matcher\" : \"exact\"," + NL +
                       "    \"value\" : \"/api/bookings\"" + NL +
                       "  } ]," + NL +
                       "  \"method\" : [ {" + NL +
                       "    \"matcher\" : \"exact\"," + NL +
                       "    \"value\" : \"GET\"" + NL +
                       "  } ]," + NL +
                       "  \"destination\" : [ {" + NL +
                       "    \"matcher\" : \"glob\"," + NL +
                       "    \"value\" : \"api*.flight.com\"" + NL +
                       "  } ]," + NL +
                       "  \"query\" : {" + NL +
                       "    \"airline\" : [ {" + NL +
                       "      \"matcher\" : \"regex\"," + NL +
                       "      \"value\" : \".*Pacific.*\"" + NL +
                       "    } ]," + NL +
                       "    \"page\" : [ {" + NL +
                       "      \"matcher\" : \"regex\"," + NL +
                       "      \"value\" : \".*\"" + NL +
                       "    } ]" + NL +
                       "  }," + NL +
                       "  \"body\" : [ {" + NL +
                       "    \"matcher\" : \"exact\"," + NL +
                       "    \"value\" : \"\"" + NL +
                       "  } ]" + NL +
                       "}" + NL +
                       NL +
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
                .body(ObjectMapperFactory.getDefaultObjectMapper().writeValueAsString(BOOKING));

        return restTemplate.exchange(bookFlightRequest, String.class);
    }
}
