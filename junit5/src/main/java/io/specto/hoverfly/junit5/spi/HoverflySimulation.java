package io.specto.hoverfly.junit5.spi;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.SimulationSource;

/**
 * Interface to be implemented for specifying Simulation in Hoverfly instance
 * For example DSL, url, file, ...
 */
public interface HoverflySimulation {

    void simulation(Hoverfly hoverfly);

    class DefaultHoverflySimulation implements HoverflySimulation {

        private Class<?> currentTest;

        public DefaultHoverflySimulation(Class<?> currentTest) {
            this.currentTest = currentTest;
        }

        @Override
        public void simulation(Hoverfly hoverfly) {
            hoverfly.importSimulation(SimulationSource.defaultPath(convertClassName()));
        }

        private String convertClassName() {
            return currentTest.getCanonicalName().replace('.', '_').replace('$', '_');
        }
    }

}
