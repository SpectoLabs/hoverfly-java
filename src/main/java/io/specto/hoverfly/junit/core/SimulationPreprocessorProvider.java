package io.specto.hoverfly.junit.core;

import io.specto.hoverfly.junit.api.HoverflyClientException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.function.Function;

public class SimulationPreprocessorProvider {
    private final boolean testInstanceRequired;
    private final Function<Object, ? extends SimulationPreprocessor> preprocessorProvider;

    private SimulationPreprocessorProvider(boolean testInstanceRequired,
                                          Function<Object, ? extends SimulationPreprocessor> preprocessorProvider) {
        this.testInstanceRequired = testInstanceRequired;
        this.preprocessorProvider = preprocessorProvider;
    }

    public boolean isTestInstanceRequired() {
        return testInstanceRequired;
    }

    public SimulationPreprocessor getSimulationPreprocessor() {
        return getSimulationPreprocessor(null);
    }

    public SimulationPreprocessor getSimulationPreprocessor(Object testInstance) {
        return preprocessorProvider.apply(testInstance);
    }

    public static SimulationPreprocessorProvider forInstance(SimulationPreprocessor preprocessor) {
        return new SimulationPreprocessorProvider(false, o -> preprocessor);
    }

    public static SimulationPreprocessorProvider forClass(Class<? extends SimulationPreprocessor> preprocessorCls) {
        if (preprocessorCls.isMemberClass() && !Modifier.isStatic(preprocessorCls.getModifiers())) {
            return new SimulationPreprocessorProvider(true, o -> createSimulationPreprocessorFromNonStaticInnerClass(o, preprocessorCls));
        }
        return forInstance(createSimulationPreprocessor(preprocessorCls));
    }

    private static SimulationPreprocessor createSimulationPreprocessor(Class<? extends SimulationPreprocessor> preprocessorCls) {
        try {
            Constructor<? extends SimulationPreprocessor> constructor =
                    preprocessorCls.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new HoverflyClientException("Failed to instantiate " + preprocessorCls.getName() + ".", e);
        }
    }

    private static SimulationPreprocessor createSimulationPreprocessorFromNonStaticInnerClass(Object testInstance,
                                                                                              Class<? extends SimulationPreprocessor> preprocessorCls)  {
        if (testInstance == null) {
            throw new IllegalStateException("TestInstance required for non-static inner class.");
        }
        try {
            Constructor<? extends SimulationPreprocessor> constructor =
                    preprocessorCls.getDeclaredConstructor(testInstance.getClass());
            constructor.setAccessible(true);
            return constructor.newInstance(testInstance);
        } catch (SecurityException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new HoverflyClientException("Failed to instantiate " + preprocessorCls.getName() + ".", e);
        }
    }

}
