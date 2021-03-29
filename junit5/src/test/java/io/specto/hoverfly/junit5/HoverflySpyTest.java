package io.specto.hoverfly.junit5;

import static org.assertj.core.api.Assertions.assertThat;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import io.specto.hoverfly.junit5.api.HoverflySpy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(HoverflyExtension.class)
@HoverflySpy(source = @HoverflySimulate.Source(value = "test-service-https.json", type = HoverflySimulate.SourceType.CLASSPATH)
)
public class HoverflySpyTest {

    @Test
    void shouldInjectCustomInstanceAsParameterWithRequiredMode(Hoverfly hoverfly) {
        assertThat(hoverfly.getMode()).isEqualTo(HoverflyMode.SPY);
    }

    @Test
    void shouldImportSimulationFromCustomSource(Hoverfly hoverfly) {
        assertThat(hoverfly.getSimulation().getHoverflyData().getPairs()).isNotEmpty();
    }
}
