package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.HoverflyConstants;
import io.specto.hoverfly.junit5.api.HoverflySimulate.SourceType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

class AutoCaptureSource {

    private Path capturePath;

    private AutoCaptureSource(Path capturePath) {
        this.capturePath = capturePath;
    }

    Path getCapturePath() {
        return capturePath;
    }

    static Optional<AutoCaptureSource> newInstance(String value, SourceType type) {

        boolean isSupportedType = type == SourceType.DEFAULT_PATH || type == SourceType.FILE;
        Path path = type == SourceType.DEFAULT_PATH ?
                Paths.get(HoverflyConstants.DEFAULT_HOVERFLY_EXPORT_PATH).resolve(value) :
                Paths.get(value);
        if (isSupportedType && !Files.isRegularFile(path)) {
            return Optional.of(new AutoCaptureSource(path));
        } else {
            return Optional.empty();
        }


    }

}
