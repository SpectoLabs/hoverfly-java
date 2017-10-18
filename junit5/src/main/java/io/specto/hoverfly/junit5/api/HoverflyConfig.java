package io.specto.hoverfly.junit5.api;


import io.specto.hoverfly.junit.core.config.LocalHoverflyConfig;
import io.specto.hoverfly.junit.core.config.RemoteHoverflyConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Customize Hoverfly when using these annotations:  {@link HoverflyCore}, {@link HoverflySimulate} and {@link HoverflyCapture}
 */
@Target(ElementType.ANNOTATION_TYPE)
public @interface HoverflyConfig {

    /**
     * The Hoverfly admin port, default is set to use any randomized free port
     */
    int adminPort() default 0;

    /**
     * The Hoverfly proxy port, default is set to use any randomized free port
     */
    int proxyPort() default 0;

    /**
     * By default Hoverfly does not proxy localhost requests, but you can enable it using this flag
     */
    boolean proxyLocalHost() default false;

    /**
     * Use this destination filter to set which hostname to simulate or capture, for example setting this to "hoverfly.io"
     * will only simulate or capture requests to "hoverfly.io"
     */
    String destination() default "";

    /**
     * By default Hoverfly does not capture any request headers, enable this flag to capture all headers
     */
    boolean captureAllHeaders() default false;

    /**
     * A list of request headers to capture
     */
    String[] captureHeaders() default {};

    /**
     * Custom SSL certificate for Hoverfly {@link LocalHoverflyConfig#sslCertificatePath(String)}
     */
    String sslCertificatePath() default "";

    /**
     * Custom SSL key for Hoverfly {@link LocalHoverflyConfig#sslKeyPath(String)}
     */
    String sslKeyPath() default "";

    /**
     * External Hoverfly instance hostname {@link RemoteHoverflyConfig#host(String)}
     */
    String remoteHost() default "";

}
