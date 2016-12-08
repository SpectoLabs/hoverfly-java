package io.specto.hoverfly.junit.rule;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.catchThrowable;

public class ClasspathResourceHoverflyRuleErrorTest {

    @Test
    public void shouldThrowExceptionWhenSubmitSimulationFailed() {

        // When
        Throwable throwable = catchThrowable(() -> HoverflyRule.inSimulationMode("test-service-v1.json").before());

        // Then
        assertThat(throwable)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Submit simulation data failed with error");
    }

}
