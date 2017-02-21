package io.specto.hoverfly.junit.core;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class HoverflyConfigTest {


    @Test
    public void shouldHaveDefaultSettings() throws Exception {
        HoverflyConfig configs = HoverflyConfig.configs();

        assertThat(configs.getHost()).isEqualTo("localhost");
        assertThat(configs.getAdminPort()).isEqualTo(0);
        assertThat(configs.getProxyPort()).isEqualTo(0);
        assertThat(configs.getSslCertificatePath()).isNull();
        assertThat(configs.getSslKeyPath()).isNull();

        assertThat(configs.isRemoteInstance()).isFalse();
        assertThat(configs.isProxyLocalHost()).isFalse();
    }


    @Test
    public void shouldHaveDefaultSettingsWhenUsingRemoteInstance() throws Exception {
        HoverflyConfig configs = HoverflyConfig.configs().useRemoteInstance();

        assertThat(configs.getHost()).isEqualTo("localhost");
        assertThat(configs.getAdminPort()).isEqualTo(0);
        assertThat(configs.getProxyPort()).isEqualTo(0);
        assertThat(configs.getSslCertificatePath()).isNull();
        assertThat(configs.getSslKeyPath()).isNull();

        assertThat(configs.isRemoteInstance()).isTrue();
        assertThat(configs.isProxyLocalHost()).isFalse();

    }

    @Test
    public void shouldBeAbleToOverrideHostNameByUseRemoteInstance() throws Exception {

        HoverflyConfig configs = HoverflyConfig.configs().useRemoteInstance("cloud-hoverfly.com");

        assertThat(configs.getHost()).isEqualTo("cloud-hoverfly.com");
        assertThat(configs.getAdminPort()).isEqualTo(0);
        assertThat(configs.getProxyPort()).isEqualTo(0);
        assertThat(configs.getSslCertificatePath()).isNull();
        assertThat(configs.getSslKeyPath()).isNull();

        assertThat(configs.isRemoteInstance()).isTrue();
        assertThat(configs.isProxyLocalHost()).isFalse();

    }
}