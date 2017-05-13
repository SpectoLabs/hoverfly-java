package io.specto.hoverfly.junit.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.model.HoverflyInfo;
import io.specto.hoverfly.junit.core.model.Simulation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

import static io.specto.hoverfly.junit.core.HoverflyMode.CAPTURE;
import static io.specto.hoverfly.junit.core.HoverflyMode.SIMULATE;
import static org.assertj.core.api.Assertions.assertThat;

public class OkHttpHoverflyClientTest {


    private Hoverfly hoverfly;
    private OkHttpHoverflyClient client;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        startDefaultHoverfly();
        client = new OkHttpHoverflyClient(hoverfly.getHoverflyConfig());
    }

    @Test
    public void shouldBeAbleToHealthcheck() throws Exception {
        assertThat(client.getHealth()).isTrue();
    }

    @Test
    public void shouldBeAbleToGetConfigInfo() throws Exception {
        HoverflyInfo configInfo = client.getConfigInfo();

        // TODO mapping string to enum for hoverfly mode
        assertThat(configInfo.getMode()).isEqualTo(SIMULATE.name().toLowerCase());
        assertThat(configInfo.getDestination()).isEqualTo(".");
    }

    @Test
    public void shouldBeAbleToSetDestination() throws Exception {

        client.setDestination("www.test.com");

        assertThat(hoverfly.getHoverflyInfo().getDestination()).isEqualTo("www.test.com");
    }


    @Test
    public void shouldBeAbleToSetMode() throws Exception {
        client.setMode(CAPTURE);

        assertThat(hoverfly.getMode()).isEqualTo(CAPTURE);
    }

    @Test
    public void shouldBeAbleToSetV1Simulation() throws Exception {
        URL resource = Resources.getResource("simulations/v1-simulation.json");
        Simulation simulation = objectMapper.readValue(resource, Simulation.class);
        client.setSimulation(simulation);

        // Then
        Simulation exportedSimulation = hoverfly.getSimulation();
        assertThat(exportedSimulation).isEqualTo(simulation);
    }


    @Test
    public void shouldBeAbleToSetV2Simulation() throws Exception {

    }

    @Test
    public void shouldBeAbleToGetSimulation() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
        if (hoverfly != null) {
            hoverfly.close();
        }
    }

    private void startDefaultHoverfly() {
        hoverfly = new Hoverfly(SIMULATE);
        hoverfly.start();
    }
}