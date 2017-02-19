package io.specto.hoverfly.junit.core;

import org.junit.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;


public class HoverflyUtilsTest {

    @Test
    public void shouldGetClasspathResource() throws Exception {

        URL resourceUrl = HoverflyUtils.findResourceOnClasspath("ssl/ca.crt");

        assertThat(resourceUrl).isNotNull();

    }
}