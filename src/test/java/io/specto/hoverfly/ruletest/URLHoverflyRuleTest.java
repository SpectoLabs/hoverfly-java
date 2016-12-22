package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import io.specto.hoverfly.webserver.ImportTestWebServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.net.URL;

import static io.specto.hoverfly.junit.core.SimulationSource.url;

public class URLHoverflyRuleTest extends BaseHoverflyRuleTest {

    private static URL url;

    // tag::urlExample[]
    @Rule
    public HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(url(url));
    // end::urlExample[]


    @BeforeClass
    public static void setUp() throws Exception {
        url = ImportTestWebServer.run();
    }


    @AfterClass
    public static void tearDown() throws Exception {
        ImportTestWebServer.terminate();
    }
}
