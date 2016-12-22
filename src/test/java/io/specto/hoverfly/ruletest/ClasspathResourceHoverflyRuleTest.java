package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;

import static io.specto.hoverfly.junit.core.SimulationSource.classpath;

public class ClasspathResourceHoverflyRuleTest {

    // tag::simulateModeQuickStart[]
    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(classpath("test-service.json"));
    // end::simulateModeQuickStart[]

}
