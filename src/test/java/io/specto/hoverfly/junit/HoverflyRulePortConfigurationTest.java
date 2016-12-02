package io.specto.hoverfly.junit;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static io.specto.hoverfly.junit.HoverflyConfig.configs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

public class HoverflyRulePortConfigurationTest {

    private static final int EXPECTED_ADMIN_PORT = 8889;
    private static final int EXPECTED_PROXY_PORT = 8890;

    // tag::portConfiguration[]
    @Rule
    public HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode("test-service.json",
            configs().proxyPort(EXPECTED_PROXY_PORT).adminPort(EXPECTED_ADMIN_PORT));
    // end::portConfiguration[]

    private RestTemplate restTemplate = new RestTemplate();

    @Test
    public void shouldStillVirtualizeServiceAfterConfiguringPorts() {
        // When
        final ResponseEntity<String> getBookingResponse = restTemplate.getForEntity("http://www.my-test.com/api/bookings/1", String.class);

        // Then
        assertThat(getBookingResponse.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldChangeAdminPortToConfiguredPort() {
        final ResponseEntity<String> health = restTemplate.getForEntity(String.format("http://localhost:%s/api/stats", EXPECTED_ADMIN_PORT), String.class);
        assertThat(health.getStatusCode()).isEqualTo(OK);
    }

}
