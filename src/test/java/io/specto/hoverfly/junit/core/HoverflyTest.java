package io.specto.hoverfly.junit.core;

import org.junit.After;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static io.specto.hoverfly.junit.core.HoverflyMode.SIMULATE;
import static org.assertj.core.api.StrictAssertions.assertThat;

public class HoverflyTest {

    private static final int EXPECTED_PROXY_PORT = 8890;
    private Hoverfly hoverfly;

    @Test
    public void hoverflyShouldStartOnConfiguredPort() throws Exception {
        hoverfly = new Hoverfly(configs().proxyPort(EXPECTED_PROXY_PORT), SIMULATE);
        hoverfly.start();
        assertThat(System.getProperty("http.proxyPort")).isEqualTo(String.valueOf(EXPECTED_PROXY_PORT));
        assertThat(hoverfly.getProxyPort()).isEqualTo(EXPECTED_PROXY_PORT);
    }

    @Test
    public void stoppingHoverflyShouldDeleteTempFiles() throws Exception {
        hoverfly = new Hoverfly(configs(), SIMULATE);
        hoverfly.start();
        hoverfly.stop();
        final Field binaryPath = ReflectionUtils.findField(Hoverfly.class, "binaryPath", Path.class);
        binaryPath.setAccessible(true);
        assertThat(Files.exists((Path) binaryPath.get(hoverfly))).isFalse();
    }

    @After
    public void tearDown() throws Exception {
        if (hoverfly != null) {
            hoverfly.stop();
        }
    }
}
