package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.created;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class HoverflyDslWithStateTest {

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(dsl(
        service("www.service-with-state.com")
            .get("/api/bookings/create")
            .willReturn(success().andSetState("hasBeenAlreadyCreated", "true"))

            .get("/api/bookings/create")
            .withState("hasBeenAlreadyCreated", "true")
            .willReturn(created())

            .get("/api/bookings/remove")
            .withState("hasBeenAlreadyCreated", "true")
            .willReturn(success().andRemoveState("hasBeenAlreadyCreated"))
    )).printSimulationData();

    private final RestTemplate restTemplate = new RestTemplate();

    @Before
    public void removeState(){
        hoverflyRule.resetState();
    }

    @Test
    public void shouldBeAbleToSetState() throws Exception {
        // When
        final ResponseEntity<Void> okResponse =
            restTemplate.getForEntity("http://www.service-with-state.com/api/bookings/create", Void.class);
        final ResponseEntity<Void> createdResponse =
            restTemplate.getForEntity("http://www.service-with-state.com/api/bookings/create", Void.class);

        // Then
        assertThat(okResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createdResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void shouldBeAbleToSetAndThenRemoveState() throws Exception {
        // given
        restTemplate.getForEntity("http://www.service-with-state.com/api/bookings/create", Void.class);

        // When
        final ResponseEntity<Void> okRemoveResponse =
            restTemplate.getForEntity("http://www.service-with-state.com/api/bookings/remove", Void.class);
        final ResponseEntity<Void> okCreateResponse =
            restTemplate.getForEntity("http://www.service-with-state.com/api/bookings/create", Void.class);

        // Then
        assertThat(okRemoveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(okCreateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldThrowAnExceptionWhenRequiredStateIsNotSet() throws Exception {
        // Then
        assertThatExceptionOfType(HttpServerErrorException.class).isThrownBy(
            () -> restTemplate.getForEntity("http://www.service-with-state.com/api/bookings/remove", Void.class))
            .withMessageContaining("502 Bad Gateway");
    }
}
