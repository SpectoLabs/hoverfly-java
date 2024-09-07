package io.specto.hoverfly.junit.core;

import io.specto.hoverfly.junit.core.config.HoverflyConfiguration;
import io.specto.hoverfly.junit.core.config.LogLevel;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Optional;

import static io.specto.hoverfly.junit.core.HoverflyConfig.localConfigs;
import static io.specto.hoverfly.junit.core.HoverflyConfig.remoteConfigs;
import static org.assertj.core.api.Assertions.assertThat;


public class HoverflyConfigTest {

    @Rule
    public EnvironmentVariables envVars = new EnvironmentVariables();

    @Test
    public void shouldHaveDefaultSettings() {

        HoverflyConfiguration configs = localConfigs().build();

        assertThat(configs.getHost()).isEqualTo("localhost");
        assertThat(configs.getScheme()).isEqualTo("http");
        assertThat(configs.isWebServer()).isFalse();
        assertThat(configs.getAdminPort()).isGreaterThan(0);
        assertThat(configs.getProxyPort()).isGreaterThan(0);
        assertThat(configs.getSslCertificatePath()).isNull();
        assertThat(configs.getSslKeyPath()).isNull();

        assertThat(configs.isRemoteInstance()).isFalse();
        assertThat(configs.isProxyLocalHost()).isFalse();
        assertThat(configs.isPlainHttpTunneling()).isFalse();
        assertThat(configs.isWebServer()).isFalse();
        assertThat(configs.isTlsVerificationDisabled()).isFalse();
        assertThat(configs.isStatefulCapture()).isFalse();
        assertThat(configs.isIncrementalCapture()).isFalse();
        assertThat(configs.getLogLevel()).isNotPresent();
        assertThat(configs.getHoverflyLogger()).isEqualTo(Optional.of(LoggerFactory.getLogger("hoverfly")));
        assertThat(configs.getClientCertPath()).isNull();
        assertThat(configs.getClientKeyPath()).isNull();
        assertThat(configs.getClientAuthDestination()).isNull();
        assertThat(configs.getClientCaCertPath()).isNull();

        assertThat(configs.getHealthCheckTimeout()).isEqualTo(Duration.ofSeconds(10));
        assertThat(configs.getHealthCheckRetryInterval()).isEqualTo(Duration.ofMillis(100));
    }

    @Test
    public void shouldHaveDefaultRemoteSettings() {
        HoverflyConfiguration configs = HoverflyConfig.remoteConfigs().build();

        assertThat(configs.getHost()).isEqualTo("localhost");
        assertThat(configs.getScheme()).isEqualTo("http");
        assertThat(configs.getAdminPort()).isEqualTo(8888);
        assertThat(configs.getProxyPort()).isEqualTo(8500);

        assertThat(configs.isRemoteInstance()).isTrue();
        assertThat(configs.isProxyLocalHost()).isFalse();
        assertThat(configs.isPlainHttpTunneling()).isFalse();
    }

    @Test
    public void shouldBeAbleToOverrideHostNameByUseRemoteInstance() {

        HoverflyConfiguration configs = remoteConfigs()
                .host("cloud-hoverfly.com")
                .build();

        assertThat(configs.getHost()).isEqualTo("cloud-hoverfly.com");

        assertThat(configs.isRemoteInstance()).isTrue();
    }

    @Test
    public void shouldSetProxyLocalHost() {
        HoverflyConfiguration configs = localConfigs().proxyLocalHost().build();

        assertThat(configs.isProxyLocalHost()).isTrue();
    }

    @Test
    public void shouldSetPlainHttpTunneling() {
        HoverflyConfiguration configs = localConfigs().plainHttpTunneling().build();

        assertThat(configs.isPlainHttpTunneling()).isTrue();
    }

    @Test
    public void shouldSetHttpsAdminEndpoint() {
        HoverflyConfiguration configs = remoteConfigs().withHttpsAdminEndpoint().build();

        assertThat(configs.getScheme()).isEqualTo("https");
        assertThat(configs.getAdminPort()).isEqualTo(443);
        assertThat(configs.getAdminCertificate()).isNull();
    }

    @Test
    public void shouldSetAuthTokenFromEnvironmentVariable() {

        envVars.set(HoverflyConstants.HOVERFLY_AUTH_TOKEN, "token-from-env");
        HoverflyConfiguration configs = remoteConfigs().withAuthHeader().build();

        assertThat(configs.getAuthToken()).isPresent();
        configs.getAuthToken().ifPresent(token -> assertThat(token).isEqualTo("token-from-env"));
    }

    @Test
    public void shouldSetAuthTokenDirectly() {
        HoverflyConfiguration configs = remoteConfigs().withAuthHeader("some-token").build();

        assertThat(configs.getAuthToken()).isPresent();
        configs.getAuthToken().ifPresent(token -> assertThat(token).isEqualTo("some-token"));
    }

    @Test
    public void shouldSetCaptureHeaders() {
        HoverflyConfiguration configs = localConfigs().captureHeaders("Accept", "Authorization").build();

        assertThat(configs.getCaptureHeaders()).hasSize(2);
        assertThat(configs.getCaptureHeaders()).containsOnly("Accept", "Authorization");
    }

    @Test
    public void shouldSetCaptureOneHeader() {
        HoverflyConfiguration configs = localConfigs().captureHeaders("Accept").build();

        assertThat(configs.getCaptureHeaders()).hasSize(1);
        assertThat(configs.getCaptureHeaders()).containsOnly("Accept");
    }

    @Test
    public void shouldSetCaptureAllHeaders() {
        HoverflyConfiguration configs = localConfigs().captureAllHeaders().build();

        assertThat(configs.getCaptureHeaders()).hasSize(1);
        assertThat(configs.getCaptureHeaders()).containsOnly("*");
    }

    @Test
    public void shouldSetWebServerMode() {
        HoverflyConfiguration configs = localConfigs().asWebServer().build();

        assertThat(configs.isWebServer()).isTrue();
    }

    @Test
    public void shouldDisableTlsVerification() {
        HoverflyConfiguration configs = localConfigs().disableTlsVerification().build();

        assertThat(configs.isTlsVerificationDisabled()).isTrue();
    }

    @Test
    public void shouldSetMiddleware() {
        HoverflyConfiguration configs = localConfigs().localMiddleware("python", "foo.py").build();

        assertThat(configs.isMiddlewareEnabled()).isTrue();
    }

    @Test
    public void shouldSetUpstreamProxy() {
        HoverflyConfiguration configs = localConfigs().upstreamProxy(new InetSocketAddress("127.0.0.1", 8900)).build();

        assertThat(configs.getUpstreamProxy()).isEqualTo("127.0.0.1:8900");
    }

    @Test
    public void shouldEnableStatefulCapture() {
        HoverflyConfiguration configs = localConfigs().enableStatefulCapture().build();

        assertThat(configs.isStatefulCapture()).isTrue();
    }

    @Test
    public void shouldEnableIncrementalCapture() {
        HoverflyConfiguration configs = localConfigs().enableIncrementalCapture().build();

        assertThat(configs.isIncrementalCapture()).isTrue();
    }


    @Test
    public void shouldAddCommands() {
        HoverflyConfiguration configs = localConfigs()
                .addCommands("-log-level", "error", "-disable-cache")
                .addCommands("-generate-ca-cert").build();

        assertThat(configs.getCommands()).containsExactly("-log-level", "error", "-disable-cache", "-generate-ca-cert");
    }

    @Test
    public void shouldSetLogLevel() {
        HoverflyConfiguration configs = localConfigs().logLevel(LogLevel.DEBUG).build();

        assertThat(configs.getLogLevel()).isEqualTo(Optional.of(LogLevel.DEBUG));
    }

    @Test
    public void shouldSetDestinations() {
        HoverflyConfiguration configs = localConfigs().destination("foo.com", "bar.com").build();

        assertThat(configs.getDestination()).isEqualTo("foo.com|bar.com");
    }

    @Test
    public void shouldSetClientAuth() {
        HoverflyConfiguration configs = localConfigs()
            .enableClientAuth("ssl/cert.pem", "ssl/key.pem")
            .build();

        assertThat(configs.getClientCertPath()).isEqualTo("ssl/cert.pem");
        assertThat(configs.getClientCertPath()).isEqualTo("ssl/cert.pem");
        assertThat(configs.getClientAuthDestination()).isEqualTo(".");
    }

    @Test
    public void shouldSetClientAuthWithDestinationFilter() {
        HoverflyConfiguration configs = localConfigs()
            .enableClientAuth("ssl/cert.pem", "ssl/key.pem", "foo.com", "bar.com")
            .build();

        assertThat(configs.getClientCertPath()).isEqualTo("ssl/cert.pem");
        assertThat(configs.getClientKeyPath()).isEqualTo("ssl/key.pem");
        assertThat(configs.getClientAuthDestination()).isEqualTo("foo.com|bar.com");
    }

    @Test
    public void shouldSetClientAuthCaCert() {
        HoverflyConfiguration configs = localConfigs()
            .clientAuthCaCertPath("ssl/ca.pem")
            .build();

        assertThat(configs.getClientCaCertPath()).isEqualTo("ssl/ca.pem");
    }

    @Test
    public void shouldSetHealthCheckTimeoutInLocalConfig() {
        HoverflyConfiguration configs = localConfigs().healthCheckTimeout(Duration.ofSeconds(20)).build();

        assertThat(configs.getHealthCheckTimeout()).isEqualTo(Duration.ofSeconds(20));
    }

    @Test
    public void shouldSetHealthCheckRetryIntervalInLocalConfig() {
        HoverflyConfiguration configs = localConfigs().healthCheckRetryInterval(Duration.ofSeconds(5)).build();

        assertThat(configs.getHealthCheckRetryInterval()).isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    public void shouldSetHealthCheckTimeoutInRemoteConfig() {
        HoverflyConfiguration configs = remoteConfigs().healthCheckTimeout(Duration.ofSeconds(20)).build();

        assertThat(configs.getHealthCheckTimeout()).isEqualTo(Duration.ofSeconds(20));
    }

    @Test
    public void shouldSetHealthCheckRetryIntervalInRemoteConfig() {
        HoverflyConfiguration configs = remoteConfigs().healthCheckRetryInterval(Duration.ofSeconds(5)).build();

        assertThat(configs.getHealthCheckRetryInterval()).isEqualTo(Duration.ofSeconds(5));
    }
}
