package io.specto.hoverfly.junit.api;

import io.specto.hoverfly.junit.api.model.ModeArguments;
import io.specto.hoverfly.junit.api.view.HoverflyInfoView;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.model.Simulation;

/**
 * Http client for querying Hoverfly admin endpoints
 */
public interface HoverflyClient {


    void setSimulation(Simulation simulation);

    Simulation getSimulation();

    HoverflyInfoView getConfigInfo();

    void setDestination(String destination);

    /**
     * Update Hoverfly mode
     * @param mode {@link HoverflyMode}
     */
    void setMode(HoverflyMode mode);

    /**
     * Update Hoverfly mode with additional arguments
     * @param mode {@link HoverflyMode}
     * @param modeArguments additional arguments such as headers to capture
     */
    void setMode(HoverflyMode mode, ModeArguments modeArguments);

    /**
     * Check Hoverfly is healthy
     * @return the status of Hoverfly
     */
    boolean getHealth();

    /**
     * Static factory method for creating a {@link HoverflyClientBuilder}
     * @return a builder for HoverflyClient
     */
    static HoverflyClientBuilder custom() {
        return new HoverflyClientBuilder();
    }

    /**
     * Static factory method for default Hoverfly client
     * @return a default HoverflyClient
     */
    static HoverflyClient createDefault() {
        return new HoverflyClientBuilder().build();
    }
}
