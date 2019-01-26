package io.specto.hoverfly.junit5.api;

import io.specto.hoverfly.junit.core.SimulationPreprocessor;
import io.specto.hoverfly.junit.core.model.Simulation;

/**
 * Default preprocessor for {@link HoverflyConfig} which signals, that no
 * preprocessor is set.
 */
public final class UnsetSimulationPreprocessor implements SimulationPreprocessor {
    private UnsetSimulationPreprocessor() {
        // Prevent creation of instances.
    }

    @Override
    public void accept(Simulation simulation) {
        throw new UnsupportedOperationException("Preprocessor must not be used. Only serves as flag for 'unset'.");
    }
}
