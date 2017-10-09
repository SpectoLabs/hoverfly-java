package io.specto.hoverfly.junit5.api;


public @interface HoverflyConfig {

    int adminPort() default 0;
    int proxyPort() default 0;
    boolean proxyLocalHost() default false;
    String destination() default "";

}
