package io.specto.hoverfly.junit.api;


import io.specto.hoverfly.junit.core.*;
import io.specto.hoverfly.junit.core.config.HoverflyConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Assertions.assertThat;

public class HoverflyClientTest {


    private Hoverfly hoverfly;
    private HoverflyConfiguration configuration;

    @Rule
    public EnvironmentVariables envVars = new EnvironmentVariables();

    @Before
    public void setUp() throws Exception {
        hoverfly = new Hoverfly(HoverflyMode.SIMULATE);
        hoverfly.start();
        configuration = hoverfly.getHoverflyConfiguration();
    }

    @Test
    public void shouldBeAbleToCreateNewInstanceOfHoverflyClient() throws Exception {


        HoverflyClient hoverflyClient = HoverflyClient.custom()
                .port(configuration.getAdminPort())
                .build();

        assertThat(hoverflyClient.getHealth()).isTrue();
    }

    @Test
    public void shouldBeAbleToCreateHoverflyClientWithAuthToken() throws Exception {
        envVars.set("HOVERFLY_AUTH_TOKEN", "some-token");
        hoverfly.importSimulation(SimulationSource.dsl(
                service("http://remote.host:12345")
                    .get("/api/health")
                        .header("Authorization", "Bearer some-token")
                    .willReturn(success())
        ));
        HoverflyClient hoverflyClient = HoverflyClient.custom()
                .host("remote.host")
                .port(12345)
                .withAuthToken()
                .build();

        assertThat(hoverflyClient.getHealth()).isTrue();
    }

    @After
    public void tearDown() throws Exception {
        if (hoverfly != null) {

            hoverfly.close();
        }
    }
}