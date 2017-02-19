package io.specto.hoverfly.junit.core;


import org.junit.Before;
import org.junit.Test;

import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static org.assertj.core.api.Assertions.assertThat;

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

}