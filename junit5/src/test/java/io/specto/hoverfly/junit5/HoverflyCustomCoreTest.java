package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflyCore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@HoverflyCore(mode = HoverflyMode.CAPTURE, config = @HoverflyConfig(adminPort = 9000, proxyPort = 9001))
@ExtendWith(HoverflyExtension.class)
class HoverflyCustomCoreTest {

    @Test
    void shouldInjectHoverflyWithCustomConfig(Hoverfly hoverfly) {
        assertThat(hoverfly.isHealthy()).isTrue();
        assertThat(hoverfly.getMode()).isEqualTo(HoverflyMode.CAPTURE);
        assertThat(hoverfly.getHoverflyConfig().getDestination()).isEqualTo("hoverfly.io");
    }

}
