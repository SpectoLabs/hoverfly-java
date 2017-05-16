package io.specto.hoverfly.junit.core;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static io.specto.hoverfly.junit.core.SystemProperty.HTTPS_PROXY_HOST;
import static io.specto.hoverfly.junit.core.SystemProperty.HTTPS_PROXY_PORT;
import static io.specto.hoverfly.junit.core.SystemProperty.HTTP_NON_PROXY_HOSTS;
import static io.specto.hoverfly.junit.core.SystemProperty.HTTP_PROXY_HOST;
import static io.specto.hoverfly.junit.core.SystemProperty.HTTP_PROXY_PORT;

class ProxyConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyConfigurer.class);

    private final HoverflyConfiguration hoverflyConfig;
    private final Map<String, String> originalProxyProperties = new HashMap<>();

    ProxyConfigurer(HoverflyConfiguration hoverflyConfig) {
        this.hoverflyConfig = hoverflyConfig;
    }

    /**
     * Configures the JVM system properties to use Hoverfly as a proxy
     */
    void setProxySystemProperties() {
        keepOriginalProxyProperties(HTTP_NON_PROXY_HOSTS, HTTP_PROXY_HOST, HTTP_PROXY_PORT, HTTPS_PROXY_HOST, HTTPS_PROXY_PORT);
        LOGGER.info("Setting proxy host to {}", hoverflyConfig.getHost());
        System.setProperty(HTTP_PROXY_HOST, hoverflyConfig.getHost());
        System.setProperty(HTTPS_PROXY_HOST, hoverflyConfig.getHost());

        if (hoverflyConfig.isProxyLocalHost()) {
            System.setProperty(HTTP_NON_PROXY_HOSTS, "");
        } else {
            System.setProperty(HTTP_NON_PROXY_HOSTS, "local|*.local|169.254/16|*.169.254/16");
        }

        if (hoverflyConfig.isRemoteInstance()) {
            String nonProxyHosts = System.getProperty(HTTP_NON_PROXY_HOSTS);
            if (StringUtils.isNotBlank(nonProxyHosts)) {
                nonProxyHosts = String.join("|", nonProxyHosts, hoverflyConfig.getHost());
            } else {
                nonProxyHosts = hoverflyConfig.getHost();
            }
            System.setProperty(HTTP_NON_PROXY_HOSTS, nonProxyHosts);
        }

        LOGGER.info("Setting proxy proxyPort to {}", hoverflyConfig.getProxyPort());

        System.setProperty(HTTP_PROXY_PORT, String.valueOf(hoverflyConfig.getProxyPort()));
        System.setProperty(HTTPS_PROXY_PORT, String.valueOf(hoverflyConfig.getProxyPort()));
    }

    void restoreProxySystemProperties() {
        for (Map.Entry<String, String> originalProperty : this.originalProxyProperties.entrySet()) {
            final String property = originalProperty.getKey();
            final String originalValue = originalProperty.getValue();
            if (originalValue == null) {
                System.clearProperty(property);
            } else {
                System.setProperty(property, originalValue);
            }
        }
        this.originalProxyProperties.clear();
    }

    private void keepOriginalProxyProperties(String... properties) {
        for (String property : properties) {
            this.originalProxyProperties.put(property, System.getProperty(property));
        }
    }
}
