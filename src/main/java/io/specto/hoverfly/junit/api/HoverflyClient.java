package io.specto.hoverfly.junit.api;

import io.specto.hoverfly.junit.core.HoverflyConfig;
import io.specto.hoverfly.junit.core.HoverflyConfiguration;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.model.HoverflyInfo;
import io.specto.hoverfly.junit.core.model.Simulation;

/**
 * Http client for querying Hoverfly admin endpoints
 */
public interface HoverflyClient {


    void setSimulation(Simulation simulation);

    Simulation getSimulation();

    HoverflyInfo getConfigInfo();

    void setDestination(String destination);

    void setMode(HoverflyMode mode);

    /**
     * Check Hoverfly is healthy
     * @return the status of Hoverfly
     */
    boolean getHealth();

    /**
     * Static factory method for creating a Hoverfly client from a HoverflyConfig object
     * @param hoverflyConfigBuilder hoverfly config fluent builder
     * @return a http client for Hoverfly admin API
     */
    static HoverflyClient newInstance(HoverflyConfig hoverflyConfigBuilder) {
        return new OkHttpHoverflyClient(hoverflyConfigBuilder.build());
    }

    /**
     * Static factory method for creating a Hoverfly client from a HoverflyConfiguration object
     * @param hoverflyConfiguration a validated Hoverfly configuration object
     * @return a http client for Hoverfly admin API
     */
    static HoverflyClient newInstance(HoverflyConfiguration hoverflyConfiguration) {
        return new OkHttpHoverflyClient(hoverflyConfiguration);
    }
}
