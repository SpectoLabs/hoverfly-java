package io.specto.hoverfly.junit;

import io.specto.hoverfly.webserver.ImportTestWebServer;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URL;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

public class HttpsHoverflyRuleTest {

    private static URL webServerUri;
    @Rule
    public HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(webServerUri);
    private RestTemplate restTemplate = new RestTemplate();

    @BeforeClass
    public static void setUp() {
        webServerUri = ImportTestWebServer.run();
    }

    @Test
    public void shouldBeAbleToGetABookingUsingHttps() {
        // When
        final ResponseEntity<String> getBookingResponse = restTemplate.getForEntity("http://www.my-test.com/api/bookings/1", String.class);

        // Then
        assertThat(getBookingResponse.getStatusCode()).isEqualTo(OK);
        assertThatJson(getBookingResponse.getBody()).isEqualTo("{" +
                "\"bookingId\":\"1\"," +
                "\"origin\":\"London\"," +
                "\"destination\":\"Singapore\"," +
                "\"time\":\"2011-09-01T12:30\"," +
                "\"_links\":{\"self\":{\"href\":\"http://localhost/api/bookings/1\"}}" +
                "}");
    }
}
