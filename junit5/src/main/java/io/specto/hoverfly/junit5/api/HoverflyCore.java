package io.specto.hoverfly.junit5.api;

import io.specto.hoverfly.junit.core.HoverflyMode;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HoverflyCore {

    HoverflyMode mode() default HoverflyMode.SIMULATE;
    HoverflyConfig config() default @HoverflyConfig;

}
