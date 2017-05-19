package io.specto.hoverfly.junit.api;


import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyConfiguration;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.SimulationSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Assertions.assertThat;

public class HoverflyClientTest {


    private Hoverfly hoverfly;
    private HoverflyConfiguration configuration;

    @Before
    public void setUp() throws Exception {
        hoverfly = new Hoverfly(HoverflyMode.SIMULATE);
        hoverfly.start();
        configuration = hoverfly.getHoverflyConfig();
    }

    @Test
    public void shouldBeAbleToCreateNewInstanceOfHoverflyClient() throws Exception {


        HoverflyClient hoverflyClient = HoverflyClient.newInstance()
                .port(configuration.getAdminPort())
                .build();

        assertThat(hoverflyClient.getHealth()).isTrue();
    }

    @Test
    public void shouldSupportDefaultConfigs() throws Exception {
        hoverfly.importSimulation(SimulationSource.dsl(
                service("http://localhost:8888")
                    .get("/api/health")
                    .willReturn(success())
        ));
        HoverflyClient hoverflyClient = HoverflyClient.newInstance().build();

        assertThat(hoverflyClient.getHealth()).isTrue();
    }

    @After
    public void tearDown() throws Exception {
        if (hoverfly != null) {

            hoverfly.close();
        }
    }
}