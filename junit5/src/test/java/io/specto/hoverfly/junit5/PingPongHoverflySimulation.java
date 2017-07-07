package io.specto.hoverfly.junit5;

import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;

import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit.dsl.ResponseCreators;
import io.specto.hoverfly.junit5.spi.HoverflySimulation;

public class PingPongHoverflySimulation implements HoverflySimulation {
    @Override
    public SimulationSource simulation() {
        return SimulationSource.dsl(
            service("www.my-test.com")
                .get("/api/bookings/1")
                .willReturn(ResponseCreators.success("{\"bookingId\":1}", "application/json")));
    }
}
