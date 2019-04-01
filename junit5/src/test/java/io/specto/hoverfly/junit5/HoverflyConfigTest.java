package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.SimulationPreprocessor;
import io.specto.hoverfly.junit.core.config.HoverflyConfiguration;
import io.specto.hoverfly.junit.core.model.Simulation;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflyCore;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;


class HoverflyConfigTest {

    @Nested
    @HoverflyCore(mode = HoverflyMode.SIMULATE, config = @HoverflyConfig())
    @ExtendWith(HoverflyExtension.class)
    class DefaultSettings {

        @Test
        void shouldUseDefaultValues(Hoverfly hoverfly) {
            HoverflyConfiguration configs = hoverfly.getHoverflyConfig();
            assertThat(configs.isPlainHttpTunneling()).isFalse();
            assertThat(configs.isMiddlewareEnabled()).isFalse();
            assertThat(configs.isProxyLocalHost()).isFalse();
            assertThat(configs.isRemoteInstance()).isFalse();
            assertThat(configs.isTlsVerificationDisabled()).isFalse();
            assertThat(configs.isWebServer()).isFalse();
            assertThat(configs.getCaptureHeaders()).isEmpty();
            assertThat(configs.getDestination()).isEmpty();
            assertThat(configs.getUpstreamProxy()).isEmpty();
            assertThat(configs.getSslCertificatePath()).isEmpty();
            assertThat(configs.getSslKeyPath()).isEmpty();
            assertThat(configs.getHost()).isEqualTo("localhost");
            assertThat(configs.getScheme()).isEqualTo("http");
            assertThat(configs.isStatefulCapture()).isFalse();
            assertThat(configs.getSimulationPreprocessor()).isEmpty();
            assertThat(configs.getCommands()).isNull();
        }
    }

    @Nested
    @HoverflyCore(mode = HoverflyMode.SIMULATE, config = @HoverflyConfig(
            proxyLocalHost = true, destination = "hoverfly.io", captureHeaders = {"Content-Type"},
            plainHttpTunneling = true, disableTlsVerification = true, upstreamProxy = "localhost:5000",
            webServer = true, statefulCapture = true,
            simulationPreprocessor = CustomSimulationPreprocessor.class,
            commands = { "-log-level", "error" }
    ))
    @ExtendWith(HoverflyExtension.class)
    class CustomizedSettings {

        @Test
        void shouldUseCustomizedValues(Hoverfly hoverfly) {
            HoverflyConfiguration configs = hoverfly.getHoverflyConfig();
            assertThat(configs.isPlainHttpTunneling()).isTrue();
            assertThat(configs.isProxyLocalHost()).isTrue();
            assertThat(configs.isTlsVerificationDisabled()).isTrue();
            assertThat(configs.isWebServer()).isTrue();
            assertThat(configs.getCaptureHeaders()).containsExactly("Content-Type");
            assertThat(configs.getDestination()).isEqualTo("hoverfly.io");
            assertThat(configs.getUpstreamProxy()).isEqualTo("localhost:5000");
            assertThat(configs.getHost()).isEqualTo("localhost");
            assertThat(configs.getScheme()).isEqualTo("http");
            assertThat(configs.isStatefulCapture()).isTrue();
            assertThat(configs.getSimulationPreprocessor()).isPresent();
            assertThat(configs.getCommands()).containsExactly("-log-level", "error");
        }

    }

    static class CustomSimulationPreprocessor implements SimulationPreprocessor {
        @Override
        public void accept(Simulation simulation) {

        }
    }
}
