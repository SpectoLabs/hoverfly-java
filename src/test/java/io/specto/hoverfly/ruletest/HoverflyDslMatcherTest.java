package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.dsl.HttpBodyConverter;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import io.specto.hoverfly.models.SimpleBooking;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.json;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.xml;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.*;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;

public class HoverflyDslMatcherTest {

    private static final SimpleBooking BOOKING = new SimpleBooking(1, "London", "Hong Kong", LocalDate.now());
    private final RestTemplate restTemplate = new RestTemplate();

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(dsl(

            // Glob Matcher for url
            service(matches("www.*-test.com"))
                    .get("/api/bookings/1")
                    .willReturn(success(json(BOOKING)))

                    // Query matcher
                    .get("/api/bookings")
                    .queryParam("airline", contains("Pacific"))
                    .queryParam("page", any())
                    .willReturn(success(json(BOOKING)))

                    // Match any query params
                    .get("/api/bookings/online")
                    .anyQueryParams()
                    .willReturn(success(json(BOOKING)))

                    // Match JSON body
                    .put("/api/bookings/1")
                    .header("Content-Type", contains("application/json"))
                    .body(equalsToJson("{\"flightId\":\"1\",\"class\":\"PREMIUM\"}"))
                    .willReturn(success())

                    .put("/api/bookings/1")
                    .body(equalsToJson(json(BOOKING)))
                    .willReturn(success())

                    // Match partial JSON body
                    .put("/api/bookings/1")
                    .header("Content-Type", contains("application/json"))
                    .body(matchesPartialJson("{\"flightId\":\"1\"}"))
                    .willReturn(success())

                    // JsonPath Matcher
                    .post("/api/bookings")
//                    .body(matchesJsonPath("$[?(@.flightId == 1)]")) // TODO: this expression is not supported
                    .body(matchesJsonPath("$.flightId"))
                    .willReturn(created("http://localhost/api/bookings/1"))

                    // Match XML body
                    .put("/api/bookings/1")
                    .body(equalsToXml("<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <flightId>1</flightId> <class>PREMIUM</class>"))
                    .willReturn(success())

                    .put("/api/bookings/1")
                    .body(equalsToXml(xml(BOOKING)))
                    .willReturn(success())

                    // XmlPath Matcher
                    .post("/api/bookings")
                    .body(matchesXPath("/flightId"))
                    .willReturn(created("http://localhost/api/bookings/1"))

                    // Match using multiple body matchers
                    .put("/api/bookings/1")
                    .header("Content-Type", contains("application/json"))
                    .body(contains("London"))
                    .body(contains("BUSINESS"))
                    .willReturn(success()),


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
    public void shouldBeAbleToQueryBookingsUsingHoverfly() {
        // When
        URI uri = UriComponentsBuilder.fromHttpUrl("http://www.my-test.com")
                .path("/api/bookings/1")
                .build()
                .toUri();
        final ResponseEntity<SimpleBooking> response = restTemplate.getForEntity(uri, SimpleBooking.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo(BOOKING);
    }

    @Test
    public void shouldFailToQueryBookingsIfUnexpectedQueryParamIsPresent() {
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
    public void shouldQueryBookingWithAnyQueryParams() {
        // When
        URI uri = UriComponentsBuilder.fromHttpUrl("http://www.my-test.com")
                .path("/api/bookings/online")
                .queryParam("class", "economy")
                .build()
                .toUri();

        final ResponseEntity<SimpleBooking> response = restTemplate.getForEntity(uri, SimpleBooking.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo(BOOKING);
    }

    @Test
    public void shouldReturn200ForAnyGetRequestWhenUsingAnyMatcher() {
        URI uri = UriComponentsBuilder.fromHttpUrl("http://www.always-success.com")
                .path("/any/api/anything")
                .build()
                .toUri();
        final ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldIgnoreHttpSchemeWhenItIsNotSet() {
        URI uri = UriComponentsBuilder.fromHttpUrl("https://www.always-success.com")
                .path("/any/api/anything")
                .build()
                .toUri();
        final ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldReturn500WhenSendingRequestWithAnyMethodToTheBookingIsDownService() {
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
    public void shouldQueryBookingWithFuzzyQueryParameters() {

        // When
        URI uri = UriComponentsBuilder.fromHttpUrl("http://www.my-test.com")
                .path("/api/bookings")
                .queryParam("airline", "Pacific Air")
                .queryParam("page", 1)
                .queryParam("size", 10)
                .build()
                .toUri();
        final ResponseEntity<SimpleBooking> response = restTemplate.getForEntity(uri, SimpleBooking.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo(BOOKING);
    }


    @Test
    public void shouldBeAbleToMatchBodyByJsonEquality() throws Exception {
        // Given
        final RequestEntity<String> bookFlightRequest = RequestEntity.put(new URI("http://www.my-test.com/api/bookings/1"))
                .contentType(APPLICATION_JSON)
                .body("{\"class\": \"PREMIUM\", \"flightId\": \"1\"}");

        // When
        final ResponseEntity<String> bookFlightResponse = restTemplate.exchange(bookFlightRequest, String.class);

        // Then
        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldBeAbleToMatchBodyByJsonEqualityWithHttpBodyConverter() throws Exception {
        // Given
        final RequestEntity<String> bookFlightRequest = RequestEntity.put(new URI("http://www.my-test.com/api/bookings/1"))
                .contentType(APPLICATION_JSON)
                .body(HttpBodyConverter.OBJECT_MAPPER.writeValueAsString(BOOKING));

        // When
        final ResponseEntity<String> bookFlightResponse = restTemplate.exchange(bookFlightRequest, String.class);

        // Then
        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldBeAbleToMatchJSONBodyPartially() throws Exception {
        // Given
        final RequestEntity<String> bookFlightRequest = RequestEntity.put(new URI("http://www.my-test.com/api/bookings/1"))
                .contentType(APPLICATION_JSON)
                .body("{\"flightId\": \"1\"}");

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
    public void shouldBeAbleToMatchBodyByXmlEqualityWithHttpBodyConverter() throws Exception {
        // Given
        final RequestEntity<String> bookFlightRequest = RequestEntity.put(new URI("http://www.my-test.com/api/bookings/1"))
                .contentType(APPLICATION_XML)
                .body(HttpBodyConverter.XML_MAPPER.writeValueAsString(BOOKING));

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

    @Test
    public void shouldBeAbleToMatchUsingMultipleBodyMatchers() throws Exception {
        // Given
        final RequestEntity<String> bookFlightRequest = RequestEntity.put(new URI("http://www.my-test.com/api/bookings/1"))
            .contentType(APPLICATION_JSON)
            .body("{\"class\": \"BUSINESS\", \"destination\": \"London\"}");

        // When
        final ResponseEntity<String> bookFlightResponse = restTemplate.exchange(bookFlightRequest, String.class);

        // Then
        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldFailedIfRequestBodyNotMatchingAllConditions() throws Exception {

        final RequestEntity<String> bookFlightRequest = RequestEntity.put(new URI("http://www.my-test.com/api/bookings/1"))
            .contentType(APPLICATION_JSON)
            .body("{\"class\": \"ECONOMY\", \"destination\": \"London\"}");   // Not matching because body does not contains 'BUSINESS'

        assertThatThrownBy(() -> restTemplate.exchange(bookFlightRequest, String.class))
            .isInstanceOf(HttpServerErrorException.class)
            .hasMessageContaining("502 Bad Gateway");

    }
}
