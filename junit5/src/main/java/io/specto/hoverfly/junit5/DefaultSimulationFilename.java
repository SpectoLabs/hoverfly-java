package io.specto.hoverfly.junit5;

public class DefaultSimulationFilename {

    private DefaultSimulationFilename(){}

    public static final String get(Class<?> testClass) {
        return testClass.getCanonicalName().replace('.', '_').replace('$', '_').concat(".json");
    }

}
