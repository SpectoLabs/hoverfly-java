package io.specto.hoverfly.ruletest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static io.specto.hoverfly.junit.core.SimulationSource.classpath;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RunWith(MockitoJUnitRunner.class)
public class NonClassRuleHoverflyRuleTest {

    private static Appender<ILoggingEvent> appender;
    @Rule
    public HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(classpath("test-service.json"));
    @Captor
    private ArgumentCaptor<LoggingEvent> logCaptor;
    private RestTemplate restTemplate = new RestTemplate();
    private Client jerseyClient = Client.create();

    @BeforeClass
    public static void init() {
        appender = Mockito.mock(Appender.class);
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(appender);
        logger.setAdditive(false);
        logger.setLevel(Level.WARN);
    }

    @Test
    public void shouldLogWarning() throws Exception {
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        LoggingEvent loggingEvent = logCaptor.getValue();
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.WARN);
        assertThat(loggingEvent.getMessage()).containsIgnoringCase("It is recommended to use HoverflyRule with @ClassRule");
    }

    @Test
    public void shouldWorkWithRestTemplate() throws URISyntaxException {
        // Given
        final RequestEntity<String> bookFlightRequest = RequestEntity.post(new URI("https://www.my-test.com/api/bookings"))
                .contentType(APPLICATION_JSON)
                .body("{\"flightId\": \"1\"}");

        // When
        final ResponseEntity<String> bookFlightResponse = restTemplate.exchange(bookFlightRequest, String.class);

        // Then
        assertThat(bookFlightResponse.getStatusCode()).isEqualTo(CREATED);
        assertThat(bookFlightResponse.getHeaders().getLocation()).isEqualTo(new URI("http://localhost/api/bookings/1"));
    }

    @Test
    public void shouldWorkWithJerseyClient() throws Exception {

        // Given
        WebResource resource = jerseyClient.resource("https://www.my-test.com/api/bookings/1");

        // When
        ClientResponse response = resource.get(ClientResponse.class);

        // Then
        assertThat(response.getStatus()).isEqualTo(OK.value());
        assertThatJson(response.getEntity(String.class)).isEqualTo("{" +
                "\"bookingId\":\"1\"," +
                "\"origin\":\"London\"," +
                "\"destination\":\"Singapore\"," +
                "\"time\":\"2011-09-01T12:30\"," +
                "\"_links\":{\"self\":{\"href\":\"http://localhost/api/bookings/1\"}}" +
                "}");
    }

}
