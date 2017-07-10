package io.specto.hoverfly.junit5.spi;

import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit5.DefaultSimulationFilename;

/**
 * Interface to be implemented for specifying Simulation in Hoverfly instance
 * For example DSL, url, file, ...
 */
public interface HoverflySimulation {

    SimulationSource simulation();

    class DefaultHoverflySimulation implements HoverflySimulation {

        private Class<?> currentTest;

        public DefaultHoverflySimulation(Class<?> currentTest) {
            this.currentTest = currentTest;
        }

        @Override
        public SimulationSource simulation() {
            return SimulationSource.defaultPath(DefaultSimulationFilename.get(this.currentTest));
        }

    }

}
