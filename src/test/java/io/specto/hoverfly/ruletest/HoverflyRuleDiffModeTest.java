package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.core.HoverflyConfig;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import io.specto.hoverfly.junit.rule.NoDiffAssertionRule;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.assertj.core.api.Assertions;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Assertions.assertThat;

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
        verifyExceptionThrownByDiffAssertion(true);
    }

    @Test
    public void shouldRecordDiffAndDiffAssertionRuleFail() throws Exception {
        verifyExceptionThrownByDiffAssertionRule(true, "assertStateApi");
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
    public void shouldRecordNoDiffWhenResponsesAreSameAndDiffAssertionRuleShouldNotFail() throws Exception {
        verifyExceptionThrownByDiffAssertionRule(false, "assertHealthApi");
    }

    @Test
    public void diffAssertionShouldResetAllRecordedDiffs() throws Exception {
        // given
        restTemplate.getForEntity(String.format("http://localhost:%s/api/v2/state", ADMIN_PROXY_PORT), Void.class);

        // when
        verifyExceptionThrownByDiffAssertion(true);

        // then
        hoverflyRule.assertThatNoDiffIsReported();
    }

    @Test
    public void diffAssertionRuleShouldResetAllRecordedDiffs() throws Exception {
        verifyExceptionThrownByDiffAssertionRule(true, "assertStateApi");
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
        verifyExceptionThrownByDiffAssertion(false);

        // then
        verifyExceptionThrownByDiffAssertion(true);
    }

    private void verifyExceptionThrownByDiffAssertion(boolean shouldReset) {
        try {
            hoverflyRule.assertThatNoDiffIsReported(shouldReset);
        } catch (Throwable t) {
            verifyExceptionAssertionErrorWithDiff(t);
            return;
        }
        Assertions.fail("Expecting code to raise a AssertionError containing a recorded diff");
    }

    private void verifyExceptionThrownByDiffAssertionRule(boolean shouldBeThrown, String methodName) {

        Request assertStateApi = Request.method(NoDiffAssertionRuleTest.class, methodName);
        Result result = new JUnitCore().run(assertStateApi);

        if (shouldBeThrown) {
            assertThat(result.getFailures()).hasSize(1);
            verifyExceptionAssertionErrorWithDiff(result.getFailures().get(0).getException());
        } else {
            assertThat(result.getFailures()).isEmpty();
        }
    }

    private void verifyExceptionAssertionErrorWithDiff(Throwable t) {
        assertThat(t)
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("method='GET'")
            .hasMessageContaining("host='localhost:54321'")
            .hasMessageContaining("path='/api/v2/state'")
            .hasMessageContaining("query=''")
            .hasMessageContaining("have been recorded 1 diff(s)")
            .hasMessageContaining("1. diff")
            .hasMessageContaining("(1.)");
    }

    public static class NoDiffAssertionRuleTest {

        @Rule
        public NoDiffAssertionRule noDiffAssertionRule = new NoDiffAssertionRule(hoverflyRule);

        private final RestTemplate restTemplate = new RestTemplate();

        @BeforeClass
        public static void skipIfNoHoverfly(){
            Throwable exception = null;
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("localhost", ADMIN_PROXY_PORT), 10);
            } catch (Throwable t) {
                exception = t;
            }
            Assume.assumeNoException(exception);
        }

        @Test
        public void assertStateApi() throws Exception {
            ResponseEntity<Void> response =
                restTemplate.getForEntity(String.format("http://localhost:%s/api/v2/state", ADMIN_PROXY_PORT),
                    Void.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        public void assertHealthApi() throws Exception {
            ResponseEntity<Void> response =
                restTemplate.getForEntity(String.format("http://localhost:%s/api/health", ADMIN_PROXY_PORT), Void.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
