package io.specto.hoverfly.junit5.api;

import io.specto.hoverfly.junit5.HoverflyExtension;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used along with {@link HoverflyExtension} to run Hoverfly in diff mode
 * By default, it tries to compare simulation file from default Hoverfly test resources path ("src/test/resources/hoverfly")
 * with filename equals to the fully qualified class name of the annotated class.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HoverflyDiff {

    /**
     * Hoverfly configurations
     * @see HoverflyConfig
     */
    HoverflyConfig config() default @HoverflyConfig;

    /**
     * Simulation source to import for comparision
     * @see HoverflySimulate.Source
     */
    HoverflySimulate.Source source() default @HoverflySimulate.Source;

}
