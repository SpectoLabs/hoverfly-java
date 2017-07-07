package io.specto.hoverfly.junit5.api;

import io.specto.hoverfly.junit5.spi.HoverflyConfigProducer;
import io.specto.hoverfly.junit5.spi.HoverflySimulation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HoverflySimulate {

    Class<? extends HoverflyConfigProducer> config() default HoverflyConfigProducer.DefaultHoverflyConfigProducer.class;
    Class<? extends HoverflySimulation> source() default HoverflySimulation.DefaultHoverflySimulation.class;

}
