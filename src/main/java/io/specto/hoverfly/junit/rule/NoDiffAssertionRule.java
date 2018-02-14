package io.specto.hoverfly.junit.rule;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyConfig;
import io.specto.hoverfly.junit.core.HoverflyMode;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Asserts that there was no diff between any of the expected responses set by simulations and the actual responses
 * returned from the real service.
 */
public class NoDiffAssertionRule implements TestRule {

    private Hoverfly hoverfly;
    private final boolean shouldClean;
    private HoverflyRule hoverflyRule;

    /**
     * Creates a rule with the given instance of {@link Hoverfly} that asserts that there was no diff between any of the
     * expected responses set by simulations and the actual responses returned from the real service.
     * The parameter {@code shouldClean} says if the rule should clean all available diffs stored in Hoverfly before the
     * test execution and when the assertion is performed (after the test method execution).
     *
     * @param hoverfly An instance of {@link Hoverfly} to be used for retrieving the diffs
     * @param shouldClean if all available diffs should be removed.
     */
    public NoDiffAssertionRule(Hoverfly hoverfly, boolean shouldClean) {
        if (hoverfly == null) {
            throw new IllegalArgumentException("Hoverfly cannot be null");
        }
        this.hoverfly = hoverfly;
        this.shouldClean = shouldClean;
    }

    /**
     * Creates a rule with the given instance of {@link HoverflyRule} that asserts that there was no diff between any of the
     * expected responses set by simulations and the actual responses returned from the real service.
     * The parameter {@code shouldClean} says if the rule should clean all available diffs stored in Hoverfly before the
     * test execution and when the assertion is performed (after the test method execution).
     *
     * @param hoverflyRule An instance of {@link HoverflyRule} to be used for retrieving the diffs
     * @param shouldClean if all available diffs should be removed.
     */
    public NoDiffAssertionRule(HoverflyRule hoverflyRule, boolean shouldClean) {
        if (hoverflyRule == null) {
            throw new IllegalArgumentException("HoverflyRule cannot be null");
        }
        this.hoverflyRule = hoverflyRule;
        this.shouldClean = shouldClean;
    }

    /**
     * Creates a rule with the given instance of {@link HoverflyConfig} that asserts that there was no diff between any of the
     * expected responses set by simulations and the actual responses returned from the real service.
     * The parameter {@code shouldClean} says if the rule should clean all available diffs stored in Hoverfly before the
     * test execution and when the assertion is performed (after the test method execution).
     *
     * @param hoverflyConfig An instance of {@link HoverflyConfig} to be used for retrieving the diffs
     * @param shouldClean if all available diffs should be removed.
     */
    public NoDiffAssertionRule(HoverflyConfig hoverflyConfig, boolean shouldClean) {
        if (hoverflyConfig == null) {
            throw new IllegalArgumentException("HoverflyConfig cannot be null");
        }
        hoverfly = new Hoverfly(hoverflyConfig, HoverflyMode.DIFF);
        this.shouldClean = shouldClean;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (shouldClean){
                    cleanDiffs();
                }
                base.evaluate();
                performAssertion();
            }
        };
    }

    private void cleanDiffs(){
        if (hoverfly != null) {
            hoverfly.resetDiffs();
        } else {
            hoverflyRule.resetDiffs();
        }
    }

    private void performAssertion(){
        if (hoverfly != null) {
            hoverfly.assertThatNoDiffIsReported(shouldClean);
        } else {
            hoverflyRule.assertThatNoDiffIsReported();
        }
    }
}
