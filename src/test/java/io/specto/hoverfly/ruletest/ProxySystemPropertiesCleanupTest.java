package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.assertj.core.api.SoftAssertions;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static io.specto.hoverfly.junit.core.SimulationSource.classpath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

public class ProxySystemPropertiesCleanupTest {

    @ClassRule
    public static RuleChain chain = RuleChain.outerRule(
            new ProvideSystemProperty("http.proxyHost", "192.168.0.1")
                                .and("http.proxyPort", "8090")
                                .and("https.proxyHost", "127.0.0.1")
                                .and("https.proxyPort", "443")
                                .and("http.nonProxyHosts", "localhost"))
            .around(new SystemPropertiesProxyResetCheckRule(
        "http.proxyHost", "192.168.0.1",  "http.proxyPort", "8090",
                    "https.proxyHost", "127.0.0.1", "https.proxyPort", "443",
                    "http.nonProxyHosts", "localhost"))
            .around(HoverflyRule.inSimulationMode(classpath("test-service.json")));

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    public void shouldBeAbleToGetABookingUsingHttps() {
        // When
        final ResponseEntity<String> getBookingResponse = restTemplate.getForEntity("https://www.my-test.com/api/bookings/1", String.class);

        // Then
        assertThat(getBookingResponse.getStatusCode()).isEqualTo(OK);
    }

    // -- Test rule to verify if properties has been preserved from before test execution

    private static class SystemPropertiesProxyResetCheckRule implements TestRule {
        private final Map<String, String> expectedProperties;
        public SystemPropertiesProxyResetCheckRule(String ... keyValuePairs) {

            expectedProperties = new HashMap<>(keyValuePairs.length / 2);
            for (int i = 0; i < keyValuePairs.length; i += 2) {
                expectedProperties.put(keyValuePairs[i], keyValuePairs[i + 1]);
            }
        }

        @Override
        public Statement apply(Statement base, Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    final Map<String, String> systemProperties = keepOriginalProxyProperties(expectedProperties.keySet());
                    base.evaluate();
                    assertPropertiesReset(systemProperties);
                }
            };
        }

        private void assertPropertiesReset(Map<String, String> systemProperties) {
            final SoftAssertions softly = new SoftAssertions();
            systemProperties.putAll(expectedProperties);
            for (Map.Entry<String, String> systemPropertyEntry : systemProperties.entrySet()) {
                softly.assertThat(System.getProperty(systemPropertyEntry.getKey())).isEqualTo(systemPropertyEntry.getValue());
            }
            softly.assertAll();
        }

        private Map<String, String> keepOriginalProxyProperties(Collection<String> properties) {
            final Map<String, String> systemProperties = new HashMap<>(properties.size());
            for (String property : properties) {
                systemProperties.put(property, System.getProperty(property));
            }
            return systemProperties;
        }
    }
}
