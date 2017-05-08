package io.specto.hoverfly.junit.core;


import org.junit.Before;
import org.junit.Test;

import static io.specto.hoverfly.junit.core.HoverflyConfig.authenticationConfigs;
import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class HoverflyConfigValidatorTest {

    private HoverflyConfigValidator validator;

    @Before
    public void setUp() throws Exception {
        validator = new HoverflyConfigValidator();
    }

    @Test
    public void shouldProvideDefaultPortForRemoteHoverflyInstanceIfNotConfigured() throws Exception {

        HoverflyConfig configs = configs().useRemoteInstance();

        HoverflyConfig validated = validator.validate(configs);


        assertThat(validated.getProxyPort()).isEqualTo(8500);
        assertThat(validated.getAdminPort()).isEqualTo(8888);
    }

    @Test
    public void shouldAssignPortForLocalHoverflyInstanceIfNotConfigured() throws Exception {
        HoverflyConfig configs = configs();

        HoverflyConfig validated = validator.validate(configs);


        assertThat(validated.getProxyPort()).isNotZero();
        assertThat(validated.getAdminPort()).isNotZero();
    }

    @Test
    public void shouldThrowExceptionIfOnlySslKeyIsConfigured() throws Exception {
        HoverflyConfig configs = configs().sslKeyPath("ssl/ca.key");

        Throwable thrown = catchThrowable(() -> validator.validate(configs));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Both SSL key and certificate files are required to override the default Hoverfly SSL");
    }

    @Test
    public void shouldThrowExceptionIfOnlySslCertIsConfigured() throws Exception {
        HoverflyConfig configs = configs().sslCertificatePath("ssl/ca.crt");

        Throwable thrown = catchThrowable(() -> validator.validate(configs));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Both SSL key and certificate files are required to override the default Hoverfly SSL");

    }

    @Test
    public void shouldThrowExceptionIfSslConfigNotEmptyWhenUsingRemoteInstance() throws Exception {
        HoverflyConfig configs = configs().useRemoteInstance()
                .sslCertificatePath("ssl/ca.crt")
                .sslKeyPath("ssl/ca.key");

        Throwable thrown = catchThrowable(() -> validator.validate(configs));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Attempt to configure SSL on remote instance is prohibited");

    }

    @Test
    public void shouldRemoveHttpSchemaFromRemoteInstanceHostName() throws Exception {

        HoverflyConfig configs = configs().useRemoteInstance("http://100.100.100.1");

        HoverflyConfig validated = validator.validate(configs);

        assertThat(validated.getHost()).isEqualTo("100.100.100.1");
    }

    @Test
    public void shouldThrowExceptionWhenHoverflyConfigIsNull() throws Exception {

        Throwable thrown = catchThrowable(() -> validator.validate(null));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HoverflyConfig cannot be null.");

    }

    @Test
    public void shouldSetDefaultHttpsAdminPortTo443() throws Exception {

        HoverflyConfig validated = validator.validate(configs().useRemoteInstance("remote-host.hoverfly.io", authenticationConfigs().withHttps("ca.cert")));

        assertThat(validated.getAdminPort()).isEqualTo(443);
    }

    @Test
    public void shouldNotChangeUserDefinedHttpsAdminPort() throws Exception {
        HoverflyConfig validated = validator.validate(configs().useRemoteInstance("remote-host.hoverfly.io", authenticationConfigs().withHttps("ca.cert")).adminPort(8443));

        assertThat(validated.getAdminPort()).isEqualTo(8443);
    }
}