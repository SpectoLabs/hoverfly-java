package io.specto.hoverfly.junit5.api;

import io.specto.hoverfly.junit5.HoverflyExtension;

import io.specto.hoverfly.junit5.api.HoverflySimulate.Source;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used along with {@link HoverflyExtension} to run Hoverfly in spy mode
 * In this mode, Hoverfly simulates external APIs if a request match is found in simulation data
 * (See Simulate mode), otherwise, the request will be passed through to the real API.
 */
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HoverflySpy {

    /**
     * Hoverfly configurations
     * @see HoverflyConfig
     */
    HoverflyConfig config() default @HoverflyConfig;

    /**
     * Simulation source to import
     * @see Source
     */
    Source source() default @Source;

}
