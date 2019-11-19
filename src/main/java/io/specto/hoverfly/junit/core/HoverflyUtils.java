/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this classpath except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2016-2016 SpectoLabs Ltd.
 */
package io.specto.hoverfly.junit.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.specto.hoverfly.junit.core.model.Simulation;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Scanner;

/**
 * Utils for Hoverfly
 */
class HoverflyUtils {

    private static ObjectWriter OBJECT_WRITER = new ObjectMapper().writerFor(Simulation.class);
    private static final ObjectReader SIMULATION_READER = new ObjectMapper().readerFor(Simulation.class);

    static void checkPortInUse(int port) {
        try (final ServerSocket ignored = new ServerSocket(port, 1, InetAddress.getLoopbackAddress())) {
            // Do nothing
        } catch (IOException e) {
            throw new IllegalStateException("Port is already in use: " + port);
        }
    }

    /**
     * Looks for a resource on the classpath with the given name
     */
    static URL findResourceOnClasspath(String resourceName) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return Optional.ofNullable(classLoader.getResource(resourceName))
                .orElseThrow(() -> new IllegalArgumentException("Resource not found with name: " + resourceName));
    }

    static InputStream getClasspathResourceAsStream(String resourceName) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return Optional.ofNullable(classLoader.getResourceAsStream(resourceName))
                .orElseThrow(() -> new IllegalArgumentException("Resource not found with name: " + resourceName));
    }


    static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    static Simulation readSimulationFromString(String simulation) {
        try {
            return SIMULATION_READER.readValue(simulation);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read simulation data.", e);
        }
    }

    static String writeSimulationAsString(Simulation simulation) {
        try {
            return OBJECT_WRITER.writeValueAsString(simulation);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize simulation object", e);
        }
    }
}
