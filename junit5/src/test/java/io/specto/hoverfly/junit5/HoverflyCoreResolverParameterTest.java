package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit5.api.HoverflyCore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@ExtendWith(HoverflyCoreResolver.class)
public class HoverflyCoreResolverParameterTest {

    private static Hoverfly hoverflyInstance;

    @Test
    public void shouldInjectHoverflyAndStartItAutomatically(@HoverflyCore Hoverfly hoverfly) {
        // We store the Hoverfly instance so it can be assert in after all as well
        hoverflyInstance = hoverfly;
        assertThat(hoverfly.getHoverflyInfo()).isNotNull();
    }

    @Test
    public void shouldInjectHoverflyInSimulateModeByDefault(@HoverflyCore Hoverfly hoverfly) {
        // We store the Hoverfly instance so it can be assert in after all as well
        hoverflyInstance = hoverfly;
        assertThat(hoverfly.getMode()).isEqualTo(HoverflyMode.SIMULATE);
    }

    @Test
    public void shouldInjectHoverflyWithConfigurationMode(@HoverflyCore(mode = HoverflyMode.CAPTURE) Hoverfly hoverfly) {
        // We store the Hoverfly instance so it can be assert in after all as well
        hoverflyInstance = hoverfly;
        assertThat(hoverfly.getMode()).isEqualTo(HoverflyMode.CAPTURE);
    }

    @AfterAll
    public static void checkHoverflyIsStopped() {
      Throwable t = catchThrowable(() -> hoverflyInstance.getHoverflyInfo());
      assertThat(t).isInstanceOf(RuntimeException.class).hasMessageContaining("Failed to connect to");
    }

}
