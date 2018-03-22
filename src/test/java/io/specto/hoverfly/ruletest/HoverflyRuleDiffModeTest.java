package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.core.HoverflyConfig;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HoverflyRuleDiffModeTest {

    private static final int ADMIN_PROXY_PORT = 54321;

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inDiffMode(dsl(
        service("http://localhost:" + ADMIN_PROXY_PORT)
            .get("/api/v2/state")
            .willReturn(success().body("expected message")),
        service("http://localhost:" + ADMIN_PROXY_PORT)
            .get("/api/health")
            .willReturn(success().body("{\"message\":\"Hoverfly is healthy\"}"))
    ), HoverflyConfig.localConfigs().proxyLocalHost().adminPort(ADMIN_PROXY_PORT)).printSimulationData();

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    public void shouldRecordDiffAndDiffAssertionFail() throws Exception {
        // when
        ResponseEntity<Void> response =
            restTemplate.getForEntity(String.format("http://localhost:%s/api/v2/state", ADMIN_PROXY_PORT), Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verifyExceptionThrownDiffAssertion(true);
    }

    @Test
    public void shouldRecordNoDiffWhenResponsesAreSameAndDiffAssertionShouldNotFail() throws Exception {
        // when
        ResponseEntity<Void> response =
            restTemplate.getForEntity(String.format("http://localhost:%s/api/health", ADMIN_PROXY_PORT), Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        hoverflyRule.assertThatNoDiffIsReported();
    }

    @Test
    public void diffAssertionShouldResetAllRecordedDiffs() throws Exception {
        // given
        restTemplate.getForEntity(String.format("http://localhost:%s/api/v2/state", ADMIN_PROXY_PORT), Void.class);

        // when
        verifyExceptionThrownDiffAssertion(true);

        // then
        hoverflyRule.assertThatNoDiffIsReported();
    }

    @Test
    public void ShouldResetAllRecordedDiffs() throws Exception {
        // given
        restTemplate.getForEntity(String.format("http://localhost:%s/api/v2/state", ADMIN_PROXY_PORT), Void.class);

        // when
        hoverflyRule.resetDiffs();

        // then
        hoverflyRule.assertThatNoDiffIsReported();
    }

    @Test
    public void diffAssertionShouldNotResetRecordedDiffs() throws Exception {
        // given
        restTemplate.getForEntity(String.format("http://localhost:%s/api/v2/state", ADMIN_PROXY_PORT), Void.class);

        // when
        verifyExceptionThrownDiffAssertion(false);

        // then
        verifyExceptionThrownDiffAssertion(true);
    }

    private void verifyExceptionThrownDiffAssertion(boolean shouldReset){
        assertThatThrownBy(() -> hoverflyRule.assertThatNoDiffIsReported(shouldReset))
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("method='GET'")
            .hasMessageContaining("host='localhost:54321'")
            .hasMessageContaining("path='/api/v2/state'")
            .hasMessageContaining("query=''")
            .hasMessageContaining("have been recorded 1 diff(s)")
            .hasMessageContaining("1. diff")
            .hasMessageContaining("(1.)");
    }
}
