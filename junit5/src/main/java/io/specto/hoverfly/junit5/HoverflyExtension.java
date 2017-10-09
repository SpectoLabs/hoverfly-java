package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflyCore;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import java.lang.reflect.AnnotatedElement;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static io.specto.hoverfly.junit.core.SimulationSource.defaultPath;

/**
 * Hoverfly Simulate Resolver. This resolver starts Hoverfly proxy server before all test methods are executed and stops it after all.
 *
 * By default Hoverfly is configured with default configuration parameters and simulation is loaded from a file located at
 * Hoverfly default path (src/test/resources/hoverfly) and file called with fully qualified name of test class, replacing dots (.) and dollar signs ($) to underlines (_).
 *
 * To configure instance just annotate test class with {@link HoverflySimulate} annotation.
 */

/**
 * Hoverfly Core resolver. This resolver starts and stops Hoverfly server, but the developer is responsible of calling
 * {@link Hoverfly#exportSimulation(Path)} or {@link Hoverfly#importSimulation(SimulationSource)}
 *
 * {@link HoverflyCore} annotation can be used to annotate a {@link Hoverfly} class at test field level with public scope or as test parameter,
 * but in both cases Hoverfly server is started before executing each test method and stopped after each test method.
 *
 * This behaviour is implemented to follow the JUnit 5 lifecycle convention of {@link TestInstancePostProcessor}
 * and {@link ParameterResolver}.
 *
 * The only exception is when Hoverfly instance is created at test field level with public and static modifiers.
 * In this case Hoverfly server is stopped at the end of test class instead of test method level.
 *
 * @see HoverflyCore
 */
public class HoverflyExtension implements BeforeEachCallback, AfterAllCallback, BeforeAllCallback, ParameterResolver {

    private Hoverfly hoverfly;
    private SimulationSource source = SimulationSource.empty();
    private HoverflyMode mode = HoverflyMode.SIMULATE;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        if (isRunning()) {
            hoverfly.reset();
            hoverfly.setMode(mode);
            if (mode == HoverflyMode.SIMULATE) {
                hoverfly.simulate(source);
            }
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {

        final Optional<AnnotatedElement> testClassElement = context.getElement();
        AnnotatedElement annotatedElement;
        if (!testClassElement.isPresent()) {
            return;
        } else {
             annotatedElement = testClassElement.get();
        }

        HoverflyConfig config = null;

        if (AnnotationSupport.isAnnotated(annotatedElement, HoverflySimulate.class)) {
            HoverflySimulate hoverflySimulate = annotatedElement.getAnnotation(HoverflySimulate.class);
            config = hoverflySimulate.config();
            if (hoverflySimulate.source().value().isEmpty()) {
                source = context.getTestClass()
                        .map(testClass -> defaultPath(DefaultSimulationFilename.get(testClass)))
                        .orElse(SimulationSource.empty());
            } else {
                source = getSimulationSource(hoverflySimulate);
            }
        } else if (AnnotationSupport.isAnnotated(annotatedElement, HoverflyCore.class)) {
            HoverflyCore hoverflyCore = annotatedElement.getAnnotation(HoverflyCore.class);
            config = hoverflyCore.config();
            mode = hoverflyCore.mode();
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
            this.hoverfly.close();
            this.hoverfly = null;
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

    private io.specto.hoverfly.junit.core.HoverflyConfig getHoverflyConfigs(HoverflyConfig config) {

        if (config != null) {
            return configs()
                    .adminPort(config.adminPort())
                    .proxyPort(config.proxyPort())
                    .destination(config.destination())
                    .proxyLocalHost(config.proxyLocalHost());

        } else {
            return configs();
        }
    }


    private SimulationSource getSimulationSource(HoverflySimulate hoverflySimulate) {
        SimulationSource source = SimulationSource.empty();
        String value = hoverflySimulate.source().value();
        switch (hoverflySimulate.source().type()) {
            case DEFAULT_PATH:
                source = defaultPath(value);
                break;
            case URL:
                source = SimulationSource.url(value);
                break;
            case CLASSPATH:
                source = SimulationSource.classpath(value);
                break;
        }
        return source;
    }
}
