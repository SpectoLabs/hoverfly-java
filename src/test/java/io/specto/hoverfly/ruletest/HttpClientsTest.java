package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static io.specto.hoverfly.junit.core.SimulationSource.classpath;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

public class HttpClientsTest {
    
    private static final String TEST_URL = "https://www.my-test.com/api/bookings/1";
    
    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(classpath("test-service.json"));

    @Test
    public void shouldWorkWithApacheHttpClient() throws Exception {

        // Given
        HttpClient httpClient = HttpClients.createSystem();
        final HttpGet httpGet = new HttpGet(TEST_URL);

        // When
        final HttpResponse response = httpClient.execute(httpGet);

        // Then
        assertJsonResponseBody(EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void shouldWorkWithSpringWebRestTemplate() throws Exception {
        // Given
        RestTemplate restTemplate = new RestTemplate();

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(TEST_URL, String.class);

        // Then
        assertJsonResponseBody(response.getBody());
    }

    @Test
    public void shouldWorkWithOkHttpClient() throws Exception {

    }

    private void assertJsonResponseBody(String body) {
        assertThatJson(body).isEqualTo("{" +
                "\"bookingId\":\"1\"," +
                "\"origin\":\"London\"," +
                "\"destination\":\"Singapore\"," +
                "\"time\":\"2011-09-01T12:30\"," +
                "\"_links\":{\"self\":{\"href\":\"http://localhost/api/bookings/1\"}}" +
                "}");
    }
}
