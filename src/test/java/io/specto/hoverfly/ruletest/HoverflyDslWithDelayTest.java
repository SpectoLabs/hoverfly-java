package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.contains;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.endsWith;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.equalsTo;
import static org.assertj.core.api.Assertions.assertThat;

public class HoverflyDslWithDelayTest {

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(dsl(

            // Global delay
            service("www.slow-service.com")
                    .get("/api/bookings")
                    .willReturn(success())

                    .andDelay(3, TimeUnit.SECONDS).forAll(),

            // Delay based on Http method (with matchers)
            service(contains("other-slow-service"))
                    .get(endsWith("/bookings"))
                    .willReturn(success())

                    .post("/api/bookings")
                    .willReturn(success())

                    .andDelay(3, TimeUnit.SECONDS).forMethod("POST"),

            // Fixed delay for a particular request
            service("www.not-so-slow-service.com")
                    .get("/api/bookings")
                    .willReturn(success().withFixedDelay(1, TimeUnit.SECONDS))

    )).printSimulationData();

    private final RestTemplate restTemplate = new RestTemplate();


    @Test
    public void shouldBeAbleToDelayRequestByHost() {

        // When
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final ResponseEntity<Void> bookingResponse = restTemplate.getForEntity("http://www.slow-service.com/api/bookings", Void.class);
        stopWatch.stop();
        long time = stopWatch.getTime();

        // Then
        assertThat(bookingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(TimeUnit.MILLISECONDS.toSeconds(time)).isGreaterThanOrEqualTo(3L);
    }

    @Test
    public void shouldBeAbleToDelayRequestByHttpMethod() {

        // When
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final ResponseEntity<Void> postResponse = restTemplate.postForEntity("http://www.other-slow-service.com/api/bookings", null, Void.class);
        stopWatch.stop();
        long postTime = stopWatch.getTime();

        // Then
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(TimeUnit.MILLISECONDS.toSeconds(postTime)).isGreaterThanOrEqualTo(3L);

        // When
        stopWatch.reset();
        stopWatch.start();
        final ResponseEntity<Void> getResponse = restTemplate.getForEntity("http://www.other-slow-service.com/api/bookings", Void.class);
        stopWatch.stop();
        long getTime = stopWatch.getTime();

        // Then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(TimeUnit.MILLISECONDS.toSeconds(getTime)).isLessThan(3L);
    }

    @Test
    public void shouldBeAbleToAddFixedDelayPerRequest() {

        // When
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final ResponseEntity<Void> getResponse = restTemplate.getForEntity("http://www.not-so-slow-service.com/api/bookings", Void.class);
        stopWatch.stop();
        long getTime = stopWatch.getTime();

        // Then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(TimeUnit.MILLISECONDS.toSeconds(getTime)).isLessThan(3L).isGreaterThanOrEqualTo(1L);
    }
}
