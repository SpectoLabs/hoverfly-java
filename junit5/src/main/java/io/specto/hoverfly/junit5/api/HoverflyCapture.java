package io.specto.hoverfly.junit5.api;

import io.specto.hoverfly.junit5.HoverflyExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used along with {@link HoverflyExtension} to run Hoverfly in capture mode
 * By default, it exports captured simulation file to default Hoverfly test resources path ("src/test/resources/hoverfly")
 * with filename equals to the fully qualified class name of the annotated class.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HoverflyCapture {

    /**
     * Hoverfly configurations
     * @see HoverflyConfig
     */
    HoverflyConfig config() default @HoverflyConfig;

    /**
     * The path for exporting the simulation file
     */
    String path() default "src/test/resources/hoverfly";

    /**
     * The name for the exported simulation file, eg. my-simulation.json
     */
    String filename() default "";

}
