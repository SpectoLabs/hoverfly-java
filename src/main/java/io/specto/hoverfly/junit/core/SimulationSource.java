package io.specto.hoverfly.junit.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.specto.hoverfly.junit.core.model.*;
import io.specto.hoverfly.junit.dsl.HoverflyDsl;
import io.specto.hoverfly.junit.dsl.StubServiceBuilder;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.specto.hoverfly.junit.core.HoverflyUtils.findResourceOnClasspath;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static jersey.repackaged.com.google.common.collect.Lists.newArrayList;


/**
 * Interface for converting a resource into a {@link Simulation}
 */
@FunctionalInterface
public interface SimulationSource {

    ObjectReader OBJECT_READER = new ObjectMapper().readerFor(Simulation.class);

    /**
     * Creates a simulation from a URL
     *
     * @param url the url of the simulation
     * @return the resource
     */
    static SimulationSource url(final URL url) {
        return () -> {
            try {
                return Optional.of(OBJECT_READER.readValue(url));
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot read simulation", e);
            }
        };
    }

    /**
     * Creates a simulation from a URL string
     *
     * @param url the url of the simulation
     * @return the resource
     */
    static SimulationSource url(final String url) {
        return () -> {
            try {
                return Optional.of(OBJECT_READER.readValue(new URL(url)));
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot read simulation", e);
            }
        };
    }

    /**
     * Creates a simulation from the classpath
     *
     * @param classpath the classpath of the simulation
     * @return the resource
     */
    static SimulationSource classpath(final String classpath) {
        return () -> {
            try {
                return Optional.of(OBJECT_READER.readValue(findResourceOnClasspath(classpath)));
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot load classpath resource: '" + classpath + "'", e);
            }
        };
    }

    /**
     * Creates a simulation from a file
     *
     * @param path the file path of the simulation
     * @return the resource
     */
    static SimulationSource file(final Path path) {
        return () -> {
            try {
                return Optional.of(OBJECT_READER.readValue(path.toFile()));
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot load file resource: '" + path.toString() + "'", e);
            }
        };
    }

    /**
     * Creates a simulation from the dsl
     * You can pass in multiple {@link StubServiceBuilder} to simulate services with different base urls
     *
     * @param stubServiceBuilder the fluent builder for {@link RequestResponsePair}
     * @return the resource
     * @see HoverflyDsl
     */
    static SimulationSource dsl(final StubServiceBuilder... stubServiceBuilder) {
        return () -> {
            final Set<RequestResponsePair> pairs = Arrays.stream(stubServiceBuilder)
                    .map(StubServiceBuilder::getRequestResponsePairs)
                    .flatMap(Set::stream)
                    .collect(toSet());

            final List<DelaySettings> delaySettings = Arrays.stream(stubServiceBuilder)
                    .map(StubServiceBuilder::getDelaySettings)
                    .flatMap(List::stream)
                    .collect(toList());

            return Optional.of(new Simulation(new HoverflyData(pairs, new GlobalActions(delaySettings)), new HoverflyMetaData()));
        };
    }

    /**
     * Creates a simulation from a {@link Simulation} object
     *
     * @param simulation the simulation
     * @return the simulation
     */
    static SimulationSource simulation(final Simulation simulation) {
        return () -> Optional.of(simulation);
    }

    /**
     * Creates no simulation
     *
     * @return an empty simulation
     */
    static SimulationSource empty() {
        return Optional::empty;
    }

    Optional<Simulation> getSimulation();
}
