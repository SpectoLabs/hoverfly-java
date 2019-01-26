/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * <p>
 * Copyright 2016-2019 SpectoLabs Ltd.
 */
package io.specto.hoverfly.junit.core;

import io.specto.hoverfly.junit.core.model.Simulation;

import java.util.function.Consumer;

/**
 * A SimulationPreprocessor processes{@link Simulation} instances prior to
 * handing them over to Hoverfly client. As {@link Simulation} instances are
 * mutable, you may directly modify the simulation be adding additional
 * request-response-pairs or weakening for example request matching of
 * previously captured session.
 */
@FunctionalInterface
public interface SimulationPreprocessor extends Consumer<Simulation> {
    /**
     * {@inheritDoc}
     * <p>
     * Allows to modify the given mutable {@link Simulation} instance,
     * by, for example, adapting request matching.
     * </p>
     */
    @Override
    void accept(Simulation simulation);
}
