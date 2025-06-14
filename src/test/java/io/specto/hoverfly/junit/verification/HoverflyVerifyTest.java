package io.specto.hoverfly.junit.verification;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.json;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.accepted;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.any;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.contains;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.equalsTo;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.equalsToJson;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.matches;
import static io.specto.hoverfly.junit.verification.HoverflyVerifications.never;
import static io.specto.hoverfly.junit.verification.HoverflyVerifications.times;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.ObjectMapperFactory;
import io.specto.hoverfly.junit.core.model.RequestFieldMatcher;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import io.specto.hoverfly.models.SimpleBooking;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class HoverflyVerifyTest {

  private static final SimpleBooking BOOKING = new SimpleBooking(1, "London", "Hong Kong", LocalDate.of(2017, 6, 29));
    private final RestTemplate restTemplate = new RestTemplate();

    private static final Hoverfly hoverfly = new Hoverfly(HoverflyMode.SIMULATE);

    @BeforeClass
    public static void beforeClass() {
        hoverfly.start();
    }

    @AfterClass
    public static void afterClass() {
        hoverfly.close();
    }

//    @Before
//    public void setUp() {
//        hoverflyRule.resetJournal();
//    }


    @Test
    public void shouldVerifyEndpoint1() {

        hoverfly.simulate(dsl(

            service(matches("api*.flight.com"))
                .get("/api/bookings/1")
                .queryParam("airline", contains("Pacific"))
                .queryParam("page", any())
                .willReturn(success(json(BOOKING)))

        ));

        ResponseEntity<SimpleBooking> response = getBookings(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        getBookings(1);

        hoverfly.verify(service(matches("*.flight.*")).get("/api/bookings/1").anyQueryParams(), times(2));

    }

    @Test
    public void shouldVerifyEndpoint2() {

        hoverfly.simulate(dsl(

            service(matches("api*.flight.com"))
                .get("/api/bookings/2")
                .queryParam("airline", contains("Pacific"))
                .queryParam("page", any())
                .willReturn(success(json(BOOKING)))

        ));

        ResponseEntity<SimpleBooking> response = getBookings(2);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        getBookings(2);

        hoverfly.verify(service(matches("*.flight.*")).get("/api/bookings/2").anyQueryParams(), times(2));

    }


    @Test
    public void shouldVerifyEndpoint3() {

        hoverfly.simulate(dsl(

            service(matches("api*.flight.com"))
                .get("/api/bookings/3")
                .queryParam("airline", contains("Pacific"))
                .queryParam("page", any())
                .willReturn(success(json(BOOKING)))

        ));

        ResponseEntity<SimpleBooking> response = getBookings(3);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        getBookings(3);

        hoverfly.verify(service(matches("*.flight.*")).get("/api/bookings/3").anyQueryParams(), times(2));

    }


    private ResponseEntity<SimpleBooking> getBookings(int id) {
        URI uri = UriComponentsBuilder.fromHttpUrl("http://api-sandbox.flight.com")
                .path("/api/bookings/" + id)
                .queryParam("airline", "Pacific Air")
                .queryParam("page", 1)
                .queryParam("size", 10)
                .build()
                .toUri();

        return restTemplate.getForEntity(uri, SimpleBooking.class);
    }


}
