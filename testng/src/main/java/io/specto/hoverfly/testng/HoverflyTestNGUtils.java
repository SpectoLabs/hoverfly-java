/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this classpath except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * <p>
 * Copyright 2016-2016 SpectoLabs Ltd.
 */
package io.specto.hoverfly.testng;

import static io.specto.hoverfly.junit.core.HoverflyConstants.DEFAULT_HOVERFLY_EXPORT_PATH;

import io.specto.hoverfly.junit.core.ObjectMapperFactory;
import io.specto.hoverfly.junit.core.model.Simulation;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import io.specto.hoverfly.testng.api.TestNGClassRule;
import io.specto.hoverfly.testng.api.TestNGRule;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.testng.IInvokedMethod;
import org.testng.ITestClass;

/**
 * Utility methods for {@link HoverflyRule}
 */
class HoverflyTestNGUtils {

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

    static void prettyPrintSimulation(Simulation value) {
        try {
            System.out.println("The following simulation is imported to Hoverfly: \n"
                    + ObjectMapperFactory.getPrettyPrinter().writeValueAsString(value));
        } catch (Exception e) {
            throw new HoverflyTestNG.HoverflyTestNgException("Failed to print simulation data: " + e.getMessage());
        }
    }

    public static HoverflyTestNG isAnnotatedWithHoverflyExtension(ITestClass iTestClass) throws IllegalAccessException {
        HoverflyTestNG hoverflyTestNG = null;
        Class testClass = iTestClass.getRealClass();
        Field[] fields = testClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(HoverflyTestNG.class)
                    && field.getAnnotation(TestNGClassRule.class) != null) {
                field.setAccessible(true);
                if (Modifier.isStatic(field.getModifiers())) {
                    hoverflyTestNG = (HoverflyTestNG) field.get(null);
                }
            }
        }
        return hoverflyTestNG;
    }

    public static HoverflyTestNG isAnnotatedWithHoverflyExtension(IInvokedMethod iInvokedMethod) throws IllegalAccessException {
        HoverflyTestNG hoverflyTestNG = null;
        if(iInvokedMethod.isTestMethod()){
            Class testClass = iInvokedMethod.getTestMethod().getRealClass();
            Object object = iInvokedMethod.getTestMethod().getInstance();
            Field[] fields = testClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType().equals(HoverflyTestNG.class)
                        && field.getAnnotation(TestNGRule.class) != null) {
                    field.setAccessible(true);
                    hoverflyTestNG = (HoverflyTestNG) field.get(object);
                }
            }
        }
        return hoverflyTestNG;
    }

    public static NoDiffAssertion isAnnotatedWithNoDiffAssertion(IInvokedMethod iInvokedMethod) throws IllegalAccessException {
        NoDiffAssertion noDiffAssertion = null;
        if(iInvokedMethod.isTestMethod()){
            Class testClass = iInvokedMethod.getTestMethod().getRealClass();
            Object object = iInvokedMethod.getTestMethod().getInstance();
            Field[] fields = testClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType().equals(NoDiffAssertion.class) && field.getAnnotation(TestNGRule.class) != null) {
                    field.setAccessible(true);
                    noDiffAssertion = (NoDiffAssertion) field.get(object);
                }
            }
        }
        return noDiffAssertion;
    }

}
