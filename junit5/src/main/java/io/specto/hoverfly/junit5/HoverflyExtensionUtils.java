package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.HoverflyConstants;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.specto.hoverfly.junit.core.HoverflyConfig.localConfigs;
import static io.specto.hoverfly.junit.core.HoverflyConfig.remoteConfigs;
import static io.specto.hoverfly.junit.core.SimulationSource.defaultPath;

class HoverflyExtensionUtils {

    private HoverflyExtensionUtils() {}

    static io.specto.hoverfly.junit.core.HoverflyConfig getHoverflyConfigs(HoverflyConfig config) {

        if (config != null) {
            io.specto.hoverfly.junit.core.HoverflyConfig configs;

            if (!config.remoteHost().isEmpty()) {
                configs = remoteConfigs().host(config.remoteHost());
            } else {
                configs = localConfigs()
                    .sslCertificatePath(config.sslCertificatePath())
                    .sslKeyPath(config.sslKeyPath());
            }
            fillHoverflyConfig(configs, config);
            return configs;

        } else {
            return localConfigs();
        }
    }

    static SimulationSource getSimulationSource(String value, HoverflySimulate.SourceType type) {
        SimulationSource source = SimulationSource.empty();
        switch (type) {
            case DEFAULT_PATH:
                source = defaultPath(value);
                break;
            case URL:
                source = SimulationSource.url(value);
                break;
            case CLASSPATH:
                source = SimulationSource.classpath(value);
                break;
            case FILE:
                source = SimulationSource.file(Paths.get(value));
                break;
        }
        return source;
    }

    static String getFileNameFromTestClass(Class<?> testClass) {
        return testClass.getCanonicalName().replace('.', '_').replace('$', '_').concat(".json");
    }

    static Path getCapturePath(String path, String filename) {

        if (path.isEmpty()) {
            path = HoverflyConstants.DEFAULT_HOVERFLY_EXPORT_PATH;
        }
        return Paths.get(path).resolve(filename);
    }

    private static void fillHoverflyConfig(io.specto.hoverfly.junit.core.HoverflyConfig configs,
        HoverflyConfig configParams) {
        configs
            .adminPort(configParams.adminPort())
            .proxyPort(configParams.proxyPort())
            .destination(configParams.destination())
            .captureHeaders(configParams.captureHeaders());
        if (configParams.proxyLocalHost()) {
            configs.proxyLocalHost();
        }
        if (configParams.captureAllHeaders()) {
            configs.captureAllHeaders();
        }
    }
}
