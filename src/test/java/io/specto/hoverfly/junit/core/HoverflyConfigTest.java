package io.specto.hoverfly.junit.core;

import org.junit.Test;

import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static org.assertj.core.api.Assertions.assertThat;


public class HoverflyConfigTest {


    @Test
    public void shouldHaveDefaultSettings() throws Exception {

        HoverflyConfiguration configs = configs().build();

        assertThat(configs.getHost()).isEqualTo("localhost");
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
        assertThat(configs.getAdminPort()).isEqualTo(8888);
        assertThat(configs.getProxyPort()).isEqualTo(8500);
        assertThat(configs.getSslCertificatePath()).isNull();
        assertThat(configs.getSslKeyPath()).isNull();

        assertThat(configs.isRemoteInstance()).isTrue();
        assertThat(configs.isProxyLocalHost()).isFalse();
    }

    @Test
    public void shouldBeAbleToOverrideHostNameByUseRemoteInstance() throws Exception {

        HoverflyConfiguration configs = configs().remote().host("cloud-hoverfly.com").build();
        assertThat(configs.getHost()).isEqualTo("cloud-hoverfly.com");

        assertThat(configs.isRemoteInstance()).isTrue();
    }

    @Test
    public void remoteHoverflyConfigShouldIgnoreCustomSslCertAndKey() throws Exception {
        HoverflyConfiguration configs = configs()
                .sslCertificatePath("ssl/ca.crt")
                .sslKeyPath("ssl/ca.key")
                .remote()
                .build();

        assertThat(configs.getSslCertificatePath()).isNull();
        assertThat(configs.getSslKeyPath()).isNull();

    }
}