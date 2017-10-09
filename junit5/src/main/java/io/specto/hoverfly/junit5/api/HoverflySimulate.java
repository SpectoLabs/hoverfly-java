package io.specto.hoverfly.junit5.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HoverflySimulate {

    HoverflyConfig config() default @HoverflyConfig;
    Source source() default @Source;

    @interface Source {
        String value() default "";
        SourceType type() default SourceType.DEFAULT_PATH;
    }

    enum SourceType {
        DEFAULT_PATH,
        CLASSPATH,
        URL;
    }
}
