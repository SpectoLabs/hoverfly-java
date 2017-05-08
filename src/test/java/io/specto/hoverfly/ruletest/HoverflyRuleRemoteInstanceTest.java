package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;
import org.junit.Ignore;

import static io.specto.hoverfly.junit.core.HoverflyConfig.authenticationConfigs;
import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static io.specto.hoverfly.junit.core.SimulationSource.classpath;

@Ignore
public class HoverflyRuleRemoteInstanceTest extends BaseHoverflyRuleTest {

    // Use a working remote hoverfly instance host
    private static final String REMOTE_HOST = "color-blind-broccoli-tommysitu.hoverfly.io";

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(classpath("test-service.json"),
            configs().useRemoteInstance(REMOTE_HOST,
                    authenticationConfigs()
                            .withHttps("ssl.cert")
                            .withProxyAuthorization("token")));
}
