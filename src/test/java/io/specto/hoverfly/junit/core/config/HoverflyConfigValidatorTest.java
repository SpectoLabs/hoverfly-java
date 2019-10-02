package io.specto.hoverfly.junit.core.config;


import org.junit.Before;
import org.junit.Test;

import static io.specto.hoverfly.junit.core.HoverflyConfig.localConfigs;
import static io.specto.hoverfly.junit.core.HoverflyConfig.remoteConfigs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// TODO some of these tests should be in HoverflyConfigTest
public class HoverflyConfigValidatorTest {

    private HoverflyConfigValidator validator;

    @Before
    public void setUp() {
        validator = new HoverflyConfigValidator();
    }

    @Test
    public void shouldProvideDefaultPortForRemoteHoverflyInstanceIfNotConfigured() {

        HoverflyConfiguration validated = remoteConfigs().build();


        assertThat(validated.getProxyPort()).isEqualTo(8500);
        assertThat(validated.getAdminPort()).isEqualTo(8888);
    }

    @Test
    public void shouldThrowExceptionIfOnlySslKeyIsConfigured() {

        assertThatThrownBy(() -> localConfigs().sslKeyPath("ssl/ca.key").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Both SSL key and certificate files are required to override the default Hoverfly SSL");
    }

    @Test
    public void shouldThrowExceptionIfOnlySslCertIsConfigured() {

        assertThatThrownBy(() -> localConfigs().sslCertificatePath("ssl/ca.crt").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Both SSL key and certificate files are required to override the default Hoverfly SSL");

    }

    @Test
    public void shouldRemoveHttpSchemaFromRemoteInstanceHostName() {

        HoverflyConfiguration validated = remoteConfigs().host("http://100.100.100.1").build();

        assertThat(validated.getHost()).isEqualTo("100.100.100.1");
    }

    @Test
    public void shouldThrowExceptionWhenHoverflyConfigIsNull() {

        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HoverflyConfig cannot be null.");

    }

    @Test
    public void shouldSetDefaultHttpsAdminPortTo443() {

        HoverflyConfiguration validated = remoteConfigs().host("remote-host.hoverfly.io").withHttpsAdminEndpoint().build();

        assertThat(validated.getAdminPort()).isEqualTo(443);
    }

    @Test
    public void shouldNotChangeUserDefinedHttpsAdminPort() {
        HoverflyConfiguration validated = remoteConfigs()
                .host("remote-host.hoverfly.io")
                .withHttpsAdminEndpoint()
                .adminPort(8443)
                .build();

        assertThat(validated.getAdminPort()).isEqualTo(8443);
    }

    @Test
    public void shouldThrowExceptionIfProxyCaCertDoesNotExist() {

        assertThatThrownBy(() -> remoteConfigs().proxyCaCert("some-cert.pem").build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Resource not found with name: some-cert.pem");
    }
}
