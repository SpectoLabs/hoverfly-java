package io.specto.hoverfly.junit.api;

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
     * Static factory method for creating a Hoverfly client builder
     * @return a builder for Hoverfly Client
     */
    static HoverflyClientBuilder newInstance() {
        return new HoverflyClientBuilder();
    }

}
