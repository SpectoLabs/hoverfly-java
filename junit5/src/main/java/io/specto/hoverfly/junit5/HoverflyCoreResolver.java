package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit5.api.HoverflyCore;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.support.AnnotationSupport;

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
public class HoverflyCoreResolver implements TestInstancePostProcessor, ParameterResolver, AfterEachCallback,
    AfterAllCallback {

    private Hoverfly hoverfly;
    private boolean isHoverflyStatic = false;

    @Override
    public boolean supports(ParameterContext parameterContext, ExtensionContext context)
        throws ParameterResolutionException {

        return parameterContext.getParameter().isAnnotationPresent(HoverflyCore.class);
    }

    @Override
    public Object resolve(ParameterContext parameterContext, ExtensionContext context)
        throws ParameterResolutionException {

        startHoverflyIfNotStarted(parameterContext.getParameter().getAnnotation(HoverflyCore.class));
        return this.hoverfly;
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        final List<Field> publicAnnotatedFields =
            AnnotationSupport.findPublicAnnotatedFields(testInstance.getClass(), Hoverfly.class, HoverflyCore.class);

        if (publicAnnotatedFields.size() == 1) {
            final Field hoveflyField = publicAnnotatedFields.get(0);

            this.isHoverflyStatic = java.lang.reflect.Modifier.isStatic(hoveflyField.getModifiers());
            startHoverflyIfNotStarted(hoveflyField.getAnnotation(HoverflyCore.class));

            hoveflyField.set(testInstance, this.hoverfly);
        }
    }

    @Override
    public void afterEach(TestExtensionContext context) throws Exception {
        if (isRunning() && !isHoverflyStatic) {
            this.hoverfly.close();
            this.hoverfly = null;
        }
    }

    @Override
    public void afterAll(ContainerExtensionContext context) throws Exception {
        if (isRunning() && isHoverflyStatic) {
            this.hoverfly.close();
            this.hoverfly = null;
        }
    }

    boolean isRunning() {
        return this.hoverfly != null;
    }

    private void startHoverflyIfNotStarted(HoverflyCore hoverflyCore) {
        if (this.hoverfly == null) {
            try {
                this.hoverfly = new Hoverfly(hoverflyCore.config().newInstance().create(),
                    hoverflyCore.mode());
                this.hoverfly.start();
            } catch (InstantiationException e) {
                throw new IllegalStateException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
