package io.specto.hoverfly.junit5.api;

import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit5.spi.HoverflyConfigProducer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface HoverflyCore {

    Class<? extends HoverflyConfigProducer> config() default HoverflyConfigProducer.DefaultHoverflyConfigProducer.class;
    HoverflyMode mode() default HoverflyMode.SIMULATE;

}
