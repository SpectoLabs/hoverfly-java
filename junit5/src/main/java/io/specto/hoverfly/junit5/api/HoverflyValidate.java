package io.specto.hoverfly.junit5.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to to verify if any discrepancy is detected.
 * Can be used at class and method level.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HoverflyValidate {

    boolean reset() default false;

}
