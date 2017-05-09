package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CaptureOrSimulationModeTest {

    @Test
    public void shouldInstantiateHoverflyInCaptureModeInCaseOfNoRecord() {

        final HoverflyRule hoverflyRule = HoverflyRule.inCaptureOrSimulationMode("mynewservicerecord.json");

        assertThat(hoverflyRule.getHoverflyMode()).isEqualTo(HoverflyMode.CAPTURE);

    }

    @Test
    public void shouldInstantiateHoverflyInSimulationModeInCaseOfPreviousRecord() {

        final HoverflyRule hoverflyRule = HoverflyRule.inCaptureOrSimulationMode("test-service-below-hoverfly-dir.json");

        assertThat(hoverflyRule.getHoverflyMode()).isEqualTo(HoverflyMode.SIMULATE);
    }

}
