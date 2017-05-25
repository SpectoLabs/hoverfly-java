package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import io.specto.hoverfly.models.SimpleBooking;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.json;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.serverError;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.any;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.startsWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

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

                    .get("/api/bookings/online")
                    .anyQueryParams()
                    .willReturn(success(json(booking))),

            // Match any path
            service("www.always-success.com")
                .get(any())
                .willReturn(success()),

            // Match any method
            service("www.booking-is-down.com")
                .anyMethod(startsWith("/api/bookings/"))
                .willReturn(serverError().body("booking is down"))


    ));



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
    public void shouldFailOnAnyMethodToBookingIsDownService() throws Exception {
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
}
