package io.specto.hoverfly.junit.dsl;

import java.util.concurrent.TimeUnit;

public class AbstractDelaySettingsBuilder {
    private final int delay;
    private final TimeUnit delayTimeUnit;


    public AbstractDelaySettingsBuilder(int delay, TimeUnit delayTimeUnit) {
        this.delay = delay;
        this.delayTimeUnit = delayTimeUnit;
    }

    protected String toPattern(String value) {
        return value;
    }

    protected int getConvertedDelay() {
        assert isValid();
        return (int) delayTimeUnit.toMillis(delay);
    }

    protected boolean isValid() {
        return delayTimeUnit != null && delay > 0;
    }
}
