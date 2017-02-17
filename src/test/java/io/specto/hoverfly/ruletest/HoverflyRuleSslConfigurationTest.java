package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import io.specto.hoverfly.models.SimpleBooking;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.sql.Date;
import java.time.LocalDate;

import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.json;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Assertions.assertThat;

public class HoverflyRuleSslConfigurationTest {

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(
            configs()
                .sslCertificatePath("ssl/ca.crt")
                .sslKeyPath("ssl/ca.key")
    );


    @Test
    public void shouldBeAbleToCallHttpsServiceEndpointUsingSelfSignedCertificate() throws Exception {

        // Given
        SimpleBooking booking = new SimpleBooking(1, "London", "Hong Kong", Date.valueOf(LocalDate.now()));
        hoverflyRule.simulate(dsl(
                service("https://my-service.com")
                    .get("/api/bookings/1")
                    .willReturn(success().body(json(booking)))
        ));

        RestTemplate restTemplate = new RestTemplate();
        // TODO configure https client

        // When
        ResponseEntity<SimpleBooking> response = restTemplate.getForEntity(UriComponentsBuilder.fromHttpUrl("https://my-service.com")
                .pathSegment("api", "bookings", "1")
                .toUriString(), SimpleBooking.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(booking);
    }
}
