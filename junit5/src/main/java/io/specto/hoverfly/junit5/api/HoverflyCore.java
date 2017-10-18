package io.specto.hoverfly.junit5.api;

import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit5.HoverflyExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use with {@link HoverflyExtension} to set mode and configuration. It does not trigger automatic import or export simulations.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HoverflyCore {
    /**
     * Hoverfly mode
     */
    HoverflyMode mode() default HoverflyMode.SIMULATE;

    /**
     * Hoverfly configurations
     * @see HoverflyConfig
     */
    HoverflyConfig config() default @HoverflyConfig;

}
