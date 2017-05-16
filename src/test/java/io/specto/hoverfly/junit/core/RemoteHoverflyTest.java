package io.specto.hoverfly.junit.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static io.specto.hoverfly.junit.core.HoverflyMode.SIMULATE;
import static io.specto.hoverfly.junit.core.SimulationSource.classpath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class RemoteHoverflyTest {

    private Hoverfly remoteHoverfly = new Hoverfly(SIMULATE);
    private Hoverfly localHoverflyDelegate;


    private RestTemplate restTemplate = new RestTemplate();

    @Before
    public void setUp() throws Exception {
        remoteHoverfly.start();
        HoverflyConfiguration remoteConfigs = remoteHoverfly.getHoverflyConfig();
        localHoverflyDelegate = new Hoverfly(configs()
                .remote()
                .adminPort(remoteConfigs.getAdminPort())
                .proxyPort(remoteConfigs.getProxyPort()), SIMULATE);
        localHoverflyDelegate.start();
        localHoverflyDelegate.importSimulation(classpath("test-service-https.json"));
    }

    @Test
    public void shouldBeAbleToMakeABookingUsingRemoteHoverfly() throws URISyntaxException {
        // Given
        final RequestEntity<String> bookFlightRequest = RequestEntity.post(new URI("https://www.my-test.com/api/bookings"))
                .contentType(APPLICATION_JSON)
                .body("{\"flightId\": \"1\"}");

        // When
        final ResponseEntity<String> bookFlightResponse = restTemplate.exchange(bookFlightRequest, String.class);

        // Then
        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(CREATED);
        assertThat(bookFlightResponse.getHeaders().getLocation()).isEqualTo(new URI("https://www.my-test.com/api/bookings/1"));
    }

    @After
    public void tearDown() throws Exception {
        if (remoteHoverfly != null) {
            remoteHoverfly.close();
        }

        if (localHoverflyDelegate != null) {
            localHoverflyDelegate.close();
        }
    }
}
