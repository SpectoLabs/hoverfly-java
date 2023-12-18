package io.specto.hoverfly.junit.dsl;

import io.specto.hoverfly.junit.core.model.DelaySettings;
import io.specto.hoverfly.junit.core.model.RequestFieldMatcher;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AbstractDelaySettingsBuilder {
    private final int delay;
    private final TimeUnit delayTimeUnit;


    public AbstractDelaySettingsBuilder(int delay, TimeUnit delayTimeUnit) {
        this.delay = delay;
        this.delayTimeUnit = delayTimeUnit;
    }

    /**
     * Convert RequestFieldMatcher list to url pattens of {@link DelaySettings}.
     *
     * @return URL patterns as string
     */
    protected String toPattern(List<RequestFieldMatcher<String>> matchers) {
        return matchers.stream().filter(m -> m.getMatcher() == RequestFieldMatcher.MatcherType.EXACT || m.getMatcher() == RequestFieldMatcher.MatcherType.REGEX)
                .findFirst()
                .map(RequestFieldMatcher::getValue)
                .map(Object::toString)
                .orElseThrow(() -> new IllegalStateException("None of the exact/regex matcher is set. "));
    }

    protected int getConvertedDelay() {
        assert isValid();
        return (int) delayTimeUnit.toMillis(delay);
    }

    protected boolean isValid() {
        return delayTimeUnit != null && delay > 0;
    }
}
