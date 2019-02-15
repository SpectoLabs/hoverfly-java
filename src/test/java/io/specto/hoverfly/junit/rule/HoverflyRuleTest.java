package io.specto.hoverfly.junit.rule;


import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit.core.model.Simulation;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.nio.file.Path;

import static io.specto.hoverfly.junit.core.SimulationSource.empty;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class HoverflyRuleTest {

    private HoverflyRule hoverflyRule;

    @Before
    public void setUp() {
    }

    @Test
    public void shouldNotImportSimulationSourceIfNoneIsSet() {
        hoverflyRule = HoverflyRule.inSimulationMode();
        Hoverfly mockHoverfly = getHoverflyMock(hoverflyRule);

        hoverflyRule.before();

        verify(mockHoverfly, never()).simulate(any());
    }

    @Test
    public void shouldBeAbleToImportMultipleSimulationSources() {
        hoverflyRule = HoverflyRule.inSimulationMode(SimulationSource.classpath("test-service.json"));
        Hoverfly mockHoverfly = getHoverflyMock(hoverflyRule);
        hoverflyRule.simulate(SimulationSource.classpath("test-service-https.json"), SimulationSource.dsl(service("foo.com").get("/").willReturn(success())));

        verify(mockHoverfly).simulate(any(SimulationSource.class), any(SimulationSource.class));
    }

    @Test
    public void shouldNotImportSimulationIfModeIsNotSimulate() {
        hoverflyRule = HoverflyRule.inCaptureMode();
        Hoverfly mockHoverfly = getHoverflyMock(hoverflyRule);

        hoverflyRule.before();

        verify(mockHoverfly, never()).importSimulation(any());
    }


    @Test
    public void shouldThrowExceptionWhenCallingSimulateOnHoverflyRuleInCaptureMode() {
        hoverflyRule = HoverflyRule.inCaptureMode();
        Hoverfly mockHoverfly = getHoverflyMock(hoverflyRule);

        assertThatThrownBy(() -> hoverflyRule.simulate(empty()))
                .isInstanceOf(HoverflyRule.HoverflyRuleException.class).hasMessageContaining("CAPTURE mode does not support this operation.");

        verifyZeroInteractions(mockHoverfly);
    }


    @Test
    public void shouldThrowExceptionWhenCallingCaptureOnHoverflyRuleInSimulateMode() {
        hoverflyRule = HoverflyRule.inSimulationMode();
        Hoverfly mockHoverfly = getHoverflyMock(hoverflyRule);

        assertThatThrownBy(() -> hoverflyRule.capture("test.json"))
                .isInstanceOf(HoverflyRule.HoverflyRuleException.class).hasMessageContaining("SIMULATE mode does not support this operation.");

        verifyZeroInteractions(mockHoverfly);
    }


    @Test
    public void shouldAlwaysShutdownHoverflyEvenTeardownOperationsFailed() {
        hoverflyRule = HoverflyRule.inCaptureMode();
        Hoverfly mockHoverfly = getHoverflyMock(hoverflyRule);
        doThrow(RuntimeException.class).when(mockHoverfly).exportSimulation(any(Path.class));

        hoverflyRule.after();

        verify(mockHoverfly).close();
    }

    @Test
    public void shouldCallResetBeforeCapture() {
        hoverflyRule = HoverflyRule.inCaptureMode();
        Hoverfly mockHoverfly = getHoverflyMock(hoverflyRule);

        hoverflyRule.capture("test.json");

        verify(mockHoverfly).reset();
    }

    @Test
    public void shouldCallResetJournalBeforeSimulate() {
        hoverflyRule = HoverflyRule.inSimulationMode();
        Hoverfly mockHoverfly = getHoverflyMock(hoverflyRule);

        hoverflyRule.simulate(empty());

        verify(mockHoverfly).resetJournal();
    }

    private Hoverfly getHoverflyMock(HoverflyRule hoverflyRule) {
        Hoverfly mockHoverfly = mock(Hoverfly.class);
        Whitebox.setInternalState(hoverflyRule, "hoverfly", mockHoverfly);
        return mockHoverfly;
    }
}