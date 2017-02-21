package io.specto.hoverfly.junit.dsl;

import io.specto.hoverfly.junit.core.model.DelaySettings;

import java.util.concurrent.TimeUnit;

public class StubServiceDelaySettingsBuilder extends AbstractDelaySettingsBuilder {

    private final StubServiceBuilder invoker;

    StubServiceDelaySettingsBuilder(int delay, TimeUnit delayTimeUnit, StubServiceBuilder invoker) {
        super(delay, delayTimeUnit);
        this.invoker = invoker;
    }

    public StubServiceBuilder forAll() {
        if (getConvertedDelay() > 0) {
            String destination = invoker.getDestination();
            invoker.addDelaySetting(new DelaySettings(toPattern(destination), getConvertedDelay(), null));
        }
        return invoker;
    }

    public StubServiceBuilder forMethod(String method) {
        if (isValid()) {
            String destination = invoker.getDestination();
            invoker.addDelaySetting(new DelaySettings(toPattern(destination), getConvertedDelay(), method));
        }
        return invoker;
    }

}
