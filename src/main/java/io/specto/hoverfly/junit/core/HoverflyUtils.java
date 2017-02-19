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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Optional;

/**
 * Utils for Hoverfly
 */
class HoverflyUtils {


    static void checkPortInUse(int port) {
        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.close();
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

}
