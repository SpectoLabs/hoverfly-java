package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit5.api.HoverflyCapture;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflyCore;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.AnnotatedElement;
import java.nio.file.Path;

import static io.specto.hoverfly.junit5.HoverflyExtensionUtils.*;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

/**
 * HoverflyExtension starts Hoverfly proxy server before all test methods are executed and stops it after all. It also reset
 * Hoverfly state between each test and restore global configurations.
 *
 * By default Hoverfly is started in simulate mode and configured with default configuration parameters. The user is responsible of calling
 * {@link Hoverfly#exportSimulation(Path)} or {@link Hoverfly#simulate(SimulationSource)}
 *
 * It implements {@link ParameterResolver} which gives you the flexibility to inject the current {@link Hoverfly} instance into JUnit 5 annotated method,
 * and make use of the Hoverfly API directly.
 *
 * {@link HoverflyCore} annotation can be used along with HoverflyExtension to set the mode and customize the configurations.
 * @see HoverflyCore for more configuration options*
 *
 * {@link HoverflySimulate} annotation can be used to instruct Hoverfly to load simulation from a file located at
 * Hoverfly default path (src/test/resources/hoverfly) and file called with fully qualified name of test class, replacing dots (.) and dollar signs ($) to underlines (_).
 * @see HoverflySimulate for more configuration options
 *
 * {@link HoverflyCapture} annotation can be used to config Hoverfly to capture and export simulation to a file located at
 * Hoverfly default path (src/test/resources/hoverfly) and file called with fully qualified name of test class, replacing dots (.) and dollar signs ($) to underlines (_).
 * @see HoverflyCapture for more configuration options*
 */
public class HoverflyExtension implements BeforeEachCallback, AfterAllCallback, BeforeAllCallback, ParameterResolver {

    private Hoverfly hoverfly;
    private SimulationSource source = SimulationSource.empty();
    private HoverflyMode mode = HoverflyMode.SIMULATE;
    private Path capturePath;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        if (isRunning()) {
            hoverfly.resetJournal();
            // Reset to per-class global configuration
            hoverfly.resetMode(mode);
            if (mode == HoverflyMode.SIMULATE) {
                hoverfly.simulate(source);
            }
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {

        AnnotatedElement annotatedElement = context.getElement().orElseThrow(() -> new IllegalStateException("No test class found."));

        HoverflyConfig config = null;

        if (isAnnotated(annotatedElement, HoverflySimulate.class)) {
            HoverflySimulate hoverflySimulate = annotatedElement.getAnnotation(HoverflySimulate.class);
            config = hoverflySimulate.config();

            HoverflySimulate.SourceType type = hoverflySimulate.source().type();
            String path = hoverflySimulate.source().value();

            if (path.isEmpty()) {
                 path = context.getTestClass()
                        .map(HoverflyExtensionUtils::getFileNameFromTestClass)
                        .orElseThrow(() -> new IllegalStateException("No test class found."));
            }

            if(hoverflySimulate.enableAutoCapture()) {
                AutoCaptureSource.newInstance(path, type).ifPresent(source -> {
                    mode = HoverflyMode.CAPTURE;
                    capturePath = source.getCapturePath();
                });
            }
            source = getSimulationSource(path, type);

        } else if (isAnnotated(annotatedElement, HoverflyCore.class)) {
            HoverflyCore hoverflyCore = annotatedElement.getAnnotation(HoverflyCore.class);
            config = hoverflyCore.config();
            mode = hoverflyCore.mode();

        } else if (isAnnotated(annotatedElement, HoverflyCapture.class)) {
            HoverflyCapture hoverflyCapture = annotatedElement.getAnnotation(HoverflyCapture.class);
            config = hoverflyCapture.config();
            mode = HoverflyMode.CAPTURE;
            String filename = hoverflyCapture.filename();
            if (filename.isEmpty()) {
                filename = context.getTestClass()
                        .map(HoverflyExtensionUtils::getFileNameFromTestClass)
                        .orElseThrow((() -> new IllegalStateException("Failed to resolve capture filename.")));
            }

            capturePath = getCapturePath(hoverflyCapture.path(), filename);
        }

        if (!isRunning()) {
            hoverfly = new Hoverfly(getHoverflyConfigs(config), mode);
            hoverfly.start();
        }

        if (mode == HoverflyMode.SIMULATE) {
            hoverfly.simulate(source);
        }
    }


    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (isRunning()) {
            try {
                if (this.capturePath != null) {
                    this.hoverfly.exportSimulation(this.capturePath);
                }
            } finally {
                this.hoverfly.close();
                this.hoverfly = null;
            }
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return Hoverfly.class.isAssignableFrom(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return this.hoverfly;
    }

    private boolean isRunning() {
        return this.hoverfly != null;
    }

}
