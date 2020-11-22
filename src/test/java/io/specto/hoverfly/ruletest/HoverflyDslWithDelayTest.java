package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.contains;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.endsWith;
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

            // Fixed delay for a particular request matcher
            service("www.not-so-slow-service.com")
                    .get("/api/bookings")
                    .willReturn(success().withFixedDelay(1, TimeUnit.SECONDS)),

            // Log Normal random delay for a particular request matcher
            service("www.random-slow-service.com")
                    .get("/api/bookings")
                    .willReturn(success().withLogNormalDelay(800, 500, 100, 1000, TimeUnit.MILLISECONDS))

    )).printSimulationData();

    private final RestTemplate restTemplate = new RestTemplate();


    @Test
    public void shouldBeAbleToDelayRequestByHost() {

        long latency = getLatency(() -> restTemplate.getForEntity("http://www.slow-service.com/api/bookings", Void.class));
        assertThat(TimeUnit.MILLISECONDS.toSeconds(latency)).isGreaterThanOrEqualTo(3L);
    }

    @Test
    public void shouldBeAbleToDelayRequestByHttpMethod() {

        long latencyForPost = getLatency(() -> restTemplate.postForEntity("http://www.other-slow-service.com/api/bookings", null, Void.class));
        assertThat(TimeUnit.MILLISECONDS.toSeconds(latencyForPost)).isGreaterThanOrEqualTo(3L);

        long latencyForGet = getLatency(() -> restTemplate.getForEntity("http://www.other-slow-service.com/api/bookings", Void.class));
        assertThat(TimeUnit.MILLISECONDS.toSeconds(latencyForGet)).isLessThan(3L);
    }

    @Test
    public void shouldBeAbleToAddFixedDelayPerRequestMatcher() {

        long latency = getLatency(() -> restTemplate.getForEntity("http://www.not-so-slow-service.com/api/bookings", Void.class));
        assertThat(TimeUnit.MILLISECONDS.toSeconds(latency)).isLessThan(3L).isGreaterThanOrEqualTo(1L);
    }

    @Test
    public void shouldBeAbleToAddLogNormalRandomDelayPerRequestMatcher() {

        long latency1 = getLatency(() -> restTemplate.getForEntity("http://www.random-slow-service.com/api/bookings", Void.class));
        long latency2 = getLatency(() -> restTemplate.getForEntity("http://www.random-slow-service.com/api/bookings", Void.class));

        assertThat(latency1).isLessThanOrEqualTo(1000).isGreaterThanOrEqualTo(100);
        assertThat(latency2).isLessThanOrEqualTo(1000).isGreaterThanOrEqualTo(100);
        assertThat(latency2).isNotEqualTo(latency1);
    }


    private long getLatency(Supplier<ResponseEntity<Void>> httpRequest) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final ResponseEntity<Void> getResponse = httpRequest.get();
        stopWatch.stop();
        long getTime = stopWatch.getTime();
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        return getTime;
    }
}
