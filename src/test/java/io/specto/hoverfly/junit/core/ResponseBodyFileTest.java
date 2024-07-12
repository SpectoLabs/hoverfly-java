package io.specto.hoverfly.junit.core;

import static io.specto.hoverfly.junit.core.HoverflyMode.SIMULATE;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.specto.hoverfly.models.SimpleBooking;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class ResponseBodyFileTest {

  private final RestTemplate restTemplate = new RestTemplate();

  @Test
  public void shouldResolveResponseBodyFilesInDefaultHoverflyFolder() throws URISyntaxException {

    try (Hoverfly hoverfly = new Hoverfly(SIMULATE)) {
      hoverfly.start();
      hoverfly.simulate(SimulationSource.defaultPath("simulation-with-body-file.json"));

      checkSimulationSuccessful();
    }
  }

  @Test
  public void shouldResolveResponseBodyFilesInTestResourcesFolder() throws URISyntaxException {

    try (Hoverfly hoverfly = new Hoverfly(HoverflyConfig.localConfigs().relativeResponseBodyFilesPath("simulations"), SIMULATE)) {
      hoverfly.start();
      hoverfly.simulate(SimulationSource.classpath("simulations/simulation-with-body-file.json"));

      checkSimulationSuccessful();
    }
  }

  @Test
  public void shouldWorksWithDsl() throws URISyntaxException {

    try (Hoverfly hoverfly = new Hoverfly(HoverflyConfig.localConfigs().relativeResponseBodyFilesPath("simulations"), SIMULATE)) {
      hoverfly.start();
      hoverfly.simulate(SimulationSource.dsl(
          service("www.my-test.com")
              .get("/api/bookings/1")
              .willReturn(success().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).bodyFile("responses/booking-200.json"))));

      checkSimulationSuccessful();
    }
  }

  @Test
  public void shouldThrowExceptionIfRelativeFilesPathNotFound() {

    assertThatThrownBy(() -> new Hoverfly(HoverflyConfig.localConfigs().relativeResponseBodyFilesPath("blahblah"), SIMULATE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Response body files path not found: blahblah");
  }

  private void checkSimulationSuccessful() throws URISyntaxException {
    RequestEntity<Void> request = RequestEntity.get(new URI("http://www.my-test.com/api/bookings/1")).build();
    ResponseEntity<SimpleBooking> response = restTemplate.exchange(request, SimpleBooking.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(new SimpleBooking(1, "London", "Singapore", LocalDate.of(2011, 9, 1 )));
  }


}
