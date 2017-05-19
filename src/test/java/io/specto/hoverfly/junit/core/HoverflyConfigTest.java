package io.specto.hoverfly.junit.core;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static org.assertj.core.api.Assertions.assertThat;


public class HoverflyConfigTest {

    @Rule
    public EnvironmentVariables envVars = new EnvironmentVariables();

    @Test
    public void shouldHaveDefaultSettings() throws Exception {

        HoverflyConfiguration configs = configs().build();

        assertThat(configs.getHost()).isEqualTo("localhost");
        assertThat(configs.getScheme()).isEqualTo("http");
        assertThat(configs.getAdminPort()).isGreaterThan(0);
        assertThat(configs.getProxyPort()).isGreaterThan(0);
        assertThat(configs.getSslCertificatePath()).isNull();
        assertThat(configs.getSslKeyPath()).isNull();

        assertThat(configs.isRemoteInstance()).isFalse();
        assertThat(configs.isProxyLocalHost()).isFalse();
    }


    @Test
    public void shouldHaveDefaultRemoteSettings() throws Exception {
        HoverflyConfiguration configs = HoverflyConfig.configs().remote().build();

        assertThat(configs.getHost()).isEqualTo("localhost");
        assertThat(configs.getScheme()).isEqualTo("http");
        assertThat(configs.getAdminPort()).isEqualTo(8888);
        assertThat(configs.getProxyPort()).isEqualTo(8500);
        assertThat(configs.getSslCertificatePath()).isNull();
        assertThat(configs.getSslKeyPath()).isNull();

        assertThat(configs.isRemoteInstance()).isTrue();
        assertThat(configs.isProxyLocalHost()).isFalse();
    }

    @Test
    public void shouldBeAbleToOverrideHostNameByUseRemoteInstance() throws Exception {

        HoverflyConfiguration configs = configs()
                .remote()
                .host("cloud-hoverfly.com")
                .build();

        assertThat(configs.getHost()).isEqualTo("cloud-hoverfly.com");

        assertThat(configs.isRemoteInstance()).isTrue();
    }

    @Test
    public void remoteHoverflyConfigShouldIgnoreCustomSslCertAndKey() throws Exception {
        HoverflyConfiguration configs = configs()
                .sslCertificatePath("ssl/ca.crt")
                .sslKeyPath("ssl/ca.key").remote()
                .remote()
                .build();

        assertThat(configs.getSslCertificatePath()).isNull();
        assertThat(configs.getSslKeyPath()).isNull();

    }

    @Test
    public void shouldSetProxyLocalHost() throws Exception {
        HoverflyConfiguration configs = configs().proxyLocalHost().build();

        assertThat(configs.isProxyLocalHost()).isTrue();
    }

    @Test
    public void shouldSetHttpsAdminEndpoint() throws Exception {
        HoverflyConfiguration configs = configs().remote().withHttpsAdminEndpoint().build();

        assertThat(configs.getScheme()).isEqualTo("https");
        assertThat(configs.getAdminPort()).isEqualTo(443);
        assertThat(configs.getAdminCertificate()).isNull();
    }

    @Test
    public void shouldSetAuthTokenFromEnvironmentVariable() throws Exception {

        envVars.set(HoverflyConstants.HOVERFLY_AUTH_TOKEN, "token-from-env");
        HoverflyConfiguration configs = configs().remote().withAuthHeader().build();

        assertThat(configs.getAuthToken()).isPresent();
        configs.getAuthToken().ifPresent(token -> assertThat(token).isEqualTo("token-from-env"));
    }

    @Test
    public void shouldSetAuthTokenDirectly() throws Exception {
        HoverflyConfiguration configs = configs().remote().withAuthHeader("some-token").build();

        assertThat(configs.getAuthToken()).isPresent();
        configs.getAuthToken().ifPresent(token -> assertThat(token).isEqualTo("some-token"));
    }
}