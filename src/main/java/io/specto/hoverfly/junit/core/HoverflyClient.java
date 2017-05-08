package io.specto.hoverfly.junit.core;

import io.specto.hoverfly.junit.core.model.HoverflyInfo;
import io.specto.hoverfly.junit.core.model.Simulation;

public interface HoverflyClient {


    void setSimulation(Simulation simulation);

    Simulation getSimulation();

    HoverflyInfo getConfigInfo();

    void setDestination(String destination);

    void setMode(HoverflyMode mode);

    boolean getHealth();

}
