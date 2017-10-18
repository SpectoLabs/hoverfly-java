package io.specto.hoverfly.junit5.api;

import io.specto.hoverfly.junit5.HoverflyExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used along with {@link HoverflyExtension} to run Hoverfly in simulate mode
 * By default, it tries to import simulation file from default Hoverfly test resources path ("src/test/resources/hoverfly")
 * with filename equals to the fully qualified class name of the annotated class.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HoverflySimulate {

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

    /**
     * Enable this flag to run Hoverfly in capture mode if simulation source is not present, otherwise, in simulate mode
     * This flag has no effect on {@link SourceType#URL} source
     */
    boolean enableAutoCapture() default false;


    /**
     * For passing static simulation source location to {@link HoverflySimulate} annotation
     */
    @interface Source {

        /**
         * The string value of source location, could be a file path or url depends on the type
         */
        String value() default "";

        /**
         * The type of the source
         */
        SourceType type() default SourceType.DEFAULT_PATH;
    }

    enum SourceType {
        DEFAULT_PATH,
        CLASSPATH,
        URL,
        FILE
    }
}
