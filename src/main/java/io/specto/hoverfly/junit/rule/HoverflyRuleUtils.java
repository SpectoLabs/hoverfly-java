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
package io.specto.hoverfly.junit.rule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.specto.hoverfly.junit.core.ObjectMapperFactory;
import io.specto.hoverfly.junit.core.model.Simulation;
import org.junit.Rule;
import org.junit.runner.Description;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.specto.hoverfly.junit.core.HoverflyConstants.DEFAULT_HOVERFLY_EXPORT_PATH;

/**
 * Utility methods for {@link HoverflyRule}
 */
class HoverflyRuleUtils {

    /**
     * Looks for a file in the src/test/resources/hoverfly directory with the given name
     */
    static Path fileRelativeToTestResourcesHoverfly(String fileName) {
        return Paths.get(DEFAULT_HOVERFLY_EXPORT_PATH).resolve(fileName);
    }

    static Path createDirectoryIfNotExist(String dirPath) {
        final Path path = Paths.get(dirPath);

        if (!existsAndIsDirectory(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return path;
    }

    private static boolean existsAndIsDirectory(Path path) {
        return Files.exists(path) && Files.isDirectory(path);
    }

    static boolean isAnnotatedWithRule(Description description) {
        boolean isRule = false;
        Field[] fields = description.getTestClass().getFields();
        for (Field field : fields) {
            if (field.getType().equals(HoverflyRule.class) && field.getAnnotation(Rule.class) != null) {
                isRule = true;
                break;
            }
        }
        return isRule;
    }

    static void prettyPrintSimulation(Simulation value) {
        try {
            System.out.println("The following simulation is imported to Hoverfly: \n"
                    + ObjectMapperFactory.getPrettyPrinter().writeValueAsString(value));
        } catch (Exception e) {
            throw new HoverflyRule.HoverflyRuleException("Failed to print simulation data: " + e.getMessage());
        }
    }

}
