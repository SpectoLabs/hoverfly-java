package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import io.specto.hoverfly.models.SimpleBooking;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.json;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.created;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.serverError;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;

public class HoverflyRuleDslMatcherTest {

    private RestTemplate restTemplate = new RestTemplate();

    private static SimpleBooking booking = new SimpleBooking(1, "London", "Hong Kong", LocalDate.now());

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(dsl(

            // Glob Matcher for url
            service(HoverflyMatchers.matches("www.*-test.com"))
                    .get("/api/bookings/1")
                    .willReturn(success(json(booking)))

                    // Query matcher
                    .get("/api/bookings")
                    .queryParam("page", any())
//                    .queryParam("airline", startsWith("Pacific"))
//                    .queryParam("airline", contains("pacific")) // not working
                    .willReturn(success(json(booking)))

                    // Match any query params
                    .get("/api/bookings/online")
                    .anyQueryParams()
                    .willReturn(success(json(booking)))

                    // Match json body
                    .put("/api/bookings/1")
                    .body(equalsToJson("{\"flightId\":\"1\",\"class\":\"PREMIUM\"}"))
                    .willReturn(success())

                    // JsonPath Matcher
                    .post("/api/bookings")
//                    .body(matchesJsonPath("$[?(@.flightId == 1)]")) // not working
                    .body(matchesJsonPath("$.flightId"))
                    .willReturn(created("http://localhost/api/bookings/1"))

                    // Match xml body
                    .put("/api/bookings/1")
                    .body(equalsToXml("<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <flightId>1</flightId> <class>PREMIUM</class>"))
                    .willReturn(success())

                    // XmlPath Matcher
                    .post("/api/bookings")
//                    .body(matchesJsonPath("?($.flightId == \"1\")")) // not working
                    .body(matchesXPath("/flightId"))
                    .willReturn(created("http://localhost/api/bookings/1")),


            // Match any path
            service("www.always-success.com")
                .get(any())
                .willReturn(success()),

            // Match any method
            service("www.booking-is-down.com")
                .anyMethod(startsWith("/api/bookings/"))
                .willReturn(serverError().body("booking is down")),

            // Match any body
            service("www.cloud-service.com")
                .post("/api/v1/containers")
                .body(any())
                .willReturn(created())

    )).printSimulationData();

    @Test
    public void shouldBeAbleToQueryBookingsUsingHoverfly() throws Exception {
        // When
        URI uri = UriComponentsBuilder.fromHttpUrl("http://www.my-test.com")
                .path("/api/bookings/1")
                .build()
                .toUri();
        final ResponseEntity<SimpleBooking> response = restTemplate.getForEntity(uri, SimpleBooking.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo(booking);
    }

    @Test
    public void shouldFailToQueryBookingsIfUnexpectedQueryParamIsPresent() throws Exception {
        // When
        URI uri = UriComponentsBuilder.fromHttpUrl("http://www.my-test.com")
                .path("/api/bookings/1")
                .queryParam("page", 1)
                .build()
                .toUri();
        Throwable throwable = catchThrowable(() -> restTemplate.getForEntity(uri, SimpleBooking.class));

        // Then
        assertThat(throwable).isInstanceOf(HttpServerErrorException.class);

        HttpServerErrorException exception = (HttpServerErrorException) throwable;

        assertThat(exception.getStatusCode()).isEqualTo(BAD_GATEWAY);
        assertThat(exception.getResponseBodyAsString()).containsIgnoringCase("Hoverfly error");
    }

    @Test
    public void shouldQueryBookingWithAnyQueryParams() throws Exception {
        // When
        URI uri = UriComponentsBuilder.fromHttpUrl("http://www.my-test.com")
                .path("/api/bookings/online")
                .queryParam("class", "economy")
                .build()
                .toUri();

        final ResponseEntity<SimpleBooking> response = restTemplate.getForEntity(uri, SimpleBooking.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo(booking);
    }

    @Test
    public void shouldReturn200ForAnyGetRequestWhenUsingAnyMatcher() throws Exception {
        URI uri = UriComponentsBuilder.fromHttpUrl("http://www.always-success.com")
                .path("/any/api/anything")
                .build()
                .toUri();
        final ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldReturn500WhenSendingRequestWithAnyMethodToTheBookingIsDownService() throws Exception {
        URI uri = UriComponentsBuilder.fromHttpUrl("http://www.booking-is-down.com")
                .path("/api/bookings/12345")
                .build()
                .toUri();
        Throwable throwable = catchThrowable(() -> restTemplate.exchange(uri, HttpMethod.DELETE, null, Void.class));

        assertThat(throwable).isInstanceOf(HttpServerErrorException.class);

        HttpServerErrorException exception = (HttpServerErrorException) throwable;

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(exception.getResponseBodyAsString()).isEqualTo("booking is down");
    }


    @Test
    public void shouldQueryBookingWithFuzzyQueryParameters() throws Exception {

        // When
        URI uri = UriComponentsBuilder.fromHttpUrl("http://www.my-test.com")
                .path("/api/bookings")
                .queryParam("page", 1)
                .queryParam("size", 10)
//                .queryParam("airline", "Pacific Air")
                .build()
                .toUri();
        final ResponseEntity<SimpleBooking> response = restTemplate.getForEntity(uri, SimpleBooking.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo(booking);
    }


    @Test
    public void shouldBeAbleToMatchBodyByJsonEquality() throws Exception {
        // Given
        final RequestEntity<String> bookFlightRequest = RequestEntity.put(new URI("http://www.my-test.com/api/bookings/1"))
                .contentType(APPLICATION_JSON)
                .body("{\"flightId\": \"1\",\"class\": \"PREMIUM\"}");

        // When
        final ResponseEntity<String> bookFlightResponse = restTemplate.exchange(bookFlightRequest, String.class);

        // Then
        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldBeAbleToMatchBodyByJsonPath() throws Exception {
        // Given
        final RequestEntity<String> bookFlightRequest = RequestEntity.post(new URI("http://www.my-test.com/api/bookings"))
                .contentType(APPLICATION_JSON)
                .body("{\"flightId\": 1}");

        // When
        final ResponseEntity<String> bookFlightResponse = restTemplate.exchange(bookFlightRequest, String.class);

        // Then
        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(CREATED);
        assertThat(bookFlightResponse.getHeaders().getLocation()).isEqualTo(new URI("http://localhost/api/bookings/1"));
    }

    @Test
    public void shouldBeAbleToMatchBodyByXmlEquality() throws Exception {
        // Given
        final RequestEntity<String> bookFlightRequest = RequestEntity.put(new URI("http://www.my-test.com/api/bookings/1"))
                .contentType(APPLICATION_XML)
                .body("<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <flightId>1</flightId> <class>PREMIUM</class>");

        // When
        final ResponseEntity<String> bookFlightResponse = restTemplate.exchange(bookFlightRequest, String.class);

        // Then
        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldBeAbleToMatchBodyByXPath() throws Exception {
        // Given
        final RequestEntity<String> bookFlightRequest = RequestEntity.post(new URI("http://www.my-test.com/api/bookings"))
                .contentType(APPLICATION_JSON)
                .body("<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <flightId>1</flightId>");

        // When
        final ResponseEntity<String> bookFlightResponse = restTemplate.exchange(bookFlightRequest, String.class);

        // Then
        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(CREATED);
        assertThat(bookFlightResponse.getHeaders().getLocation()).isEqualTo(new URI("http://localhost/api/bookings/1"));
    }


    @Test
    public void shouldBeAbleToMatchAnyBody() throws Exception {
        // Given
        final RequestEntity<String> bookFlightRequest = RequestEntity.post(new URI("http://www.cloud-service.com/api/v1/containers"))
                .contentType(APPLICATION_JSON)
                .body("{ \"Hostname\": \"\", \"Domainname\": \"\", \"User\": \"\"}");

        // When
        final ResponseEntity<String> bookFlightResponse = restTemplate.exchange(bookFlightRequest, String.class);

        // Then
        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(CREATED);
    }
}
