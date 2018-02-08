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
    public void setUp() throws Exception {
        validator = new HoverflyConfigValidator();
    }

    @Test
    public void shouldProvideDefaultPortForRemoteHoverflyInstanceIfNotConfigured() throws Exception {

        HoverflyConfiguration validated = remoteConfigs().build();


        assertThat(validated.getProxyPort()).isEqualTo(8500);
        assertThat(validated.getAdminPort()).isEqualTo(8888);
    }

    @Test
    public void shouldAssignPortForLocalHoverflyInstanceIfNotConfigured() throws Exception {

        HoverflyConfiguration validated = localConfigs().build();


        assertThat(validated.getProxyPort()).isNotZero();
        assertThat(validated.getAdminPort()).isNotZero();
    }

    @Test
    public void shouldThrowExceptionIfOnlySslKeyIsConfigured() throws Exception {

        assertThatThrownBy(() -> localConfigs().sslKeyPath("ssl/ca.key").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Both SSL key and certificate files are required to override the default Hoverfly SSL");
    }

    @Test
    public void shouldThrowExceptionIfOnlySslCertIsConfigured() throws Exception {

        assertThatThrownBy(() -> localConfigs().sslCertificatePath("ssl/ca.crt").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Both SSL key and certificate files are required to override the default Hoverfly SSL");

    }

    @Test
    public void shouldRemoveHttpSchemaFromRemoteInstanceHostName() throws Exception {

        HoverflyConfiguration validated = remoteConfigs().host("http://100.100.100.1").build();

        assertThat(validated.getHost()).isEqualTo("100.100.100.1");
    }

    @Test
    public void shouldThrowExceptionWhenHoverflyConfigIsNull() throws Exception {

        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HoverflyConfig cannot be null.");

    }

    @Test
    public void shouldSetDefaultHttpsAdminPortTo443() throws Exception {

        HoverflyConfiguration validated = remoteConfigs().host("remote-host.hoverfly.io").withHttpsAdminEndpoint().build();

        assertThat(validated.getAdminPort()).isEqualTo(443);
    }

    @Test
    public void shouldNotChangeUserDefinedHttpsAdminPort() throws Exception {
        HoverflyConfiguration validated = remoteConfigs()
                .host("remote-host.hoverfly.io")
                .withHttpsAdminEndpoint()
                .adminPort(8443)
                .build();

        assertThat(validated.getAdminPort()).isEqualTo(8443);
    }

    @Test
    public void shouldThrowExceptionIfProxyCaCertDoesNotExist() throws Exception {

        assertThatThrownBy(() -> remoteConfigs().proxyCaCert("some-cert.pem").build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Resource not found with name: some-cert.pem");
    }
}