package io.specto.hoverfly.junit.api;


import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.*;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Assertions.assertThat;

public class HoverflyClientTest {


    @Rule
    public EnvironmentVariables envVars = new EnvironmentVariables();

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(configs().proxyLocalHost());

    @Test
    public void shouldBeAbleToCreateNewInstanceOfHoverflyClient() throws Exception {

        hoverflyRule.simulate(dsl(
                service("localhost:9999")
                    .get("/api/health")
                    .willReturn(success())
        ));
        HoverflyClient hoverflyClient = HoverflyClient.custom()
                .port(9999)
                .build();

        assertThat(hoverflyClient.getHealth()).isTrue();
    }

    @Test
    public void shouldBeAbleToCreateHoverflyClientWithAuthToken() throws Exception {
        envVars.set("HOVERFLY_AUTH_TOKEN", "some-token");
        hoverflyRule.simulate(dsl(
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

    @Test
    public void shouldCreateDefaultClient() throws Exception {
        hoverflyRule.simulate(dsl(
                service("localhost:8888")
                    .get("/api/health")
                    .willReturn(success())
        ));
        HoverflyClient defaultClient = HoverflyClient.createDefault();

        assertThat(defaultClient.getHealth()).isTrue();
    }

}