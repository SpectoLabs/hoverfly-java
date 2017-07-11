package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyConfig;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit5.api.HoverflyCapture;
import io.specto.hoverfly.junit5.spi.HoverflyConfigProducer;
import java.lang.reflect.AnnotatedElement;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

import static io.specto.hoverfly.junit.core.SimulationSource.empty;


/**
 * Hoverfly Capture Resolver. This resolver starts Hoverfly proxy server before all test methods are executed and stops it after all.
 *
 * By default Hoverfly is configured with default configuration parameters and captured data is stored from a file located at
 * Hoverfly default path (src/test/resources/hoverfly) and file called with fully qualified name of test class, replacing dots (.) and dollar signs ($) to underlines (_).
 *
 * To configure instance just annotate test class with {@link HoverflyCapture} annotation.
 */
public class HoverflyCaptureResolver implements BeforeAllCallback, AfterAllCallback, ParameterResolver {

    private Hoverfly hoverfly;
    private Path capturePath;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {

        final Optional<AnnotatedElement> testClassElement = context.getElement();

        if (testClassElement.isPresent() && AnnotationSupport.isAnnotated(testClassElement.get(), HoverflyCapture.class)) {
            final Optional<HoverflyCapture> hoverflyCaptureOptional =
                AnnotationSupport.findAnnotation(testClassElement.get(), HoverflyCapture.class);

            final Optional<Class<?>> testClass = context.getTestClass();
            if (testClass.isPresent()) {
                final HoverflyCapture hoverflyCapture = hoverflyCaptureOptional.get();
                final HoverflyConfigProducer hoverflyConfigProducer = hoverflyCapture.config().newInstance();
                startHoverflyIfNotStarted(testClass.get(), hoverflyConfigProducer.create(), hoverflyCapture.path(), hoverflyCapture.recordFile());
            }

        } else {
            final Optional<Class<?>> testClass = context.getTestClass();
            if (testClass.isPresent()) {
                startHoverflyIfNotStarted(testClass.get(), HoverflyConfig.configs(), HoverflyCapture.PATH, HoverflyCapture.NO_RECORD_FILE);
            }
        }
    }

    private void startHoverflyIfNotStarted(Class<?> currentTest, HoverflyConfig config, String path, String recordFile) {
        if (!isRunning()) {
            this.hoverfly = new Hoverfly(config, HoverflyMode.CAPTURE);
            this.hoverfly.start();

            if (capturePath != null) {
                hoverfly.exportSimulation(capturePath);
            }

            hoverfly.importSimulation(empty());
            capturePath = fileRelativeToTestResourcesHoverfly(path, recordFile, currentTest);
        }
    }

    private Path fileRelativeToTestResourcesHoverfly(String path, String recordFile, Class<?> testClass) {
        String filename = recordFile;
        if (HoverflyCapture.NO_RECORD_FILE.equals(recordFile)) {
            filename = DefaultSimulationFilename.get(testClass);
        }

        return Paths.get(path, filename);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        try {
            this.hoverfly.exportSimulation(this.capturePath);
        } finally {
            this.hoverfly.close();
            this.hoverfly = null;
        }
    }

    boolean isRunning() {
        return this.hoverfly != null;
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
}
