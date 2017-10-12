package io.specto.hoverfly.junit5.api;


import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
public @interface HoverflyConfig {

    int adminPort() default 0;
    int proxyPort() default 0;
    boolean proxyLocalHost() default false;
    String destination() default "";
    boolean captureAllHeaders() default false;
    String[] captureHeaders() default {};

    String sslCertificatePath() default "";
    String sslKeyPath() default "";
    String remoteHost() default "";

}
