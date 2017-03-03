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

import org.junit.Rule;
import org.junit.runner.Description;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility methods for {@link HoverflyRule}
 */
class HoverflyRuleUtils {

    public static final String SRC_TEST_RESOURCES_HOVERFLY = "src/test/resources/hoverfly";

    /**
     * Looks for a file in the src/test/resources/hoverfly directory with the given name
     */
    static Path fileRelativeToTestResourcesHoverfly(String fileName) {
        return Paths.get(SRC_TEST_RESOURCES_HOVERFLY, fileName);
    }

    /**
     * Creates src/test/resources/hoverfly directory if it does not exist
     */
    static void createTestResourcesHoverflyDirectoryIfNoneExisting() {
        final Path path = Paths.get(SRC_TEST_RESOURCES_HOVERFLY);

        if (! existsAndIsDirectory(path)) {
            // Delete in case src/test/resources/hoverfly is a file
            try {
                Files.deleteIfExists(path);
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean existsAndIsDirectory(Path path) {
        return Files.exists(path) && Files.isDirectory(path);
    }

    static boolean isAnnotatedWithRule(Description description) {
        boolean isRule = false;
        Field[] fields = description.getTestClass().getFields();
        for (Field field : fields) {
            if (field.getType().isAssignableFrom(HoverflyRule.class) && field.getAnnotation(Rule.class) != null) {
                isRule = true;
                break;
            }
        }
        return isRule;
    }

}
