package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import io.specto.hoverfly.models.SimpleBooking;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;

import static io.specto.hoverfly.junit.core.HoverflyConfig.localConfigs;
import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.json;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Assertions.assertThat;

public class HoverflyRuleSslConfigurationTest {

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(
        localConfigs()
                .sslCertificatePath("ssl/ca.crt")
                .sslKeyPath("ssl/ca.key")
    );

    @Test
    public void shouldBeAbleToCallHttpsServiceEndpointUsingSelfSignedCertificate() throws Exception {

        // Given
        SimpleBooking booking = new SimpleBooking(1, "London", "Hong Kong", LocalDate.now());
        hoverflyRule.simulate(dsl(
                service("https://my-service.com")
                    .get("/api/bookings/1")
                    .willReturn(success().body(json(booking)))
        ));

        RestTemplate restTemplate = new RestTemplate();
        CloseableHttpClient httpsClient = HttpClients.custom().useSystemProperties().setSSLContext(hoverflyRule.getSslConfigurer().getSslContext()).build();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpsClient));

        // When
        ResponseEntity<SimpleBooking> response = restTemplate.getForEntity(UriComponentsBuilder.fromHttpUrl("https://my-service.com")
                .pathSegment("api", "bookings", "1")
                .toUriString(), SimpleBooking.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(booking);
    }
}
