package io.specto.hoverfly.junit5.api;

import io.specto.hoverfly.junit5.spi.HoverflyConfigProducer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HoverflyCapture {

    String NO_RECORD_FILE = "";
    String PATH = "src/test/resources/hoverfly";

    Class<? extends HoverflyConfigProducer> config() default HoverflyConfigProducer.DefaultHoverflyConfigProducer.class;
    String path() default PATH;
    String recordFile() default NO_RECORD_FILE;

}
