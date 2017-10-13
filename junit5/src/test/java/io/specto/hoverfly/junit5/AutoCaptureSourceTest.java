package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AutoCaptureSourceTest {


    @Test
    void shouldReturnEmptyOptionalIfSourceTypeIsNotSupported() {
        Optional<AutoCaptureSource> autoCaptureSource = AutoCaptureSource.newInstance("test-service-https.json", HoverflySimulate.SourceType.CLASSPATH);

        assertThat(autoCaptureSource).isNotPresent();
    }

    @Test
    void shouldReturnEmptyOptionalIfSourceExists() {
        Optional<AutoCaptureSource> autoCaptureSource = AutoCaptureSource.newInstance("src/test/resources/test-service-https.json", HoverflySimulate.SourceType.FILE);

        assertThat(autoCaptureSource).isNotPresent();
    }

    @Test
    void shouldReturnAutoCaptureSource() {
        Optional<AutoCaptureSource> autoCaptureSource = AutoCaptureSource.newInstance("new-simulation.json", HoverflySimulate.SourceType.DEFAULT_PATH);

        assertThat(autoCaptureSource).isPresent();

        assertThat(autoCaptureSource.get().getCapturePath()).isEqualTo(Paths.get("src/test/resources/hoverfly/new-simulation.json"));
    }
}