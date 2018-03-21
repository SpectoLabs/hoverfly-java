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
    private HoverflyRule hoverflyRule;

    /**
     * Creates a rule with the given instance of {@link Hoverfly} that asserts that there was no diff between any of the
     * expected responses set by simulations and the actual responses returned from the real service.
     * The rule also removes (before and after the test execution) all possible diffs that are stored in Hoverfly.
     * This ensures that all test runs are executed in isolated and clean environment.
     *
     * @param hoverfly
     *     An instance of {@link Hoverfly} to be used for retrieving the diffs
     */
    public NoDiffAssertionRule(Hoverfly hoverfly) {
        if (hoverfly == null) {
            throw new IllegalArgumentException("Hoverfly cannot be null");
        }
        this.hoverfly = hoverfly;
    }

    /**
     * Creates a rule with the given instance of {@link HoverflyRule} that asserts that there was no diff between any of the
     * expected responses set by simulations and the actual responses returned from the real service.
     * The rule also removes (before and after the test execution) all possible diffs that are stored in Hoverfly.
     * This ensures that all test runs are executed in isolated and clean environment.
     *
     * @param hoverflyRule
     *     An instance of {@link HoverflyRule} to be used for retrieving the diffs
     */
    public NoDiffAssertionRule(HoverflyRule hoverflyRule) {
        if (hoverflyRule == null) {
            throw new IllegalArgumentException("HoverflyRule cannot be null");
        }
        this.hoverflyRule = hoverflyRule;
    }

    /**
     * Creates a rule with the given instance of {@link HoverflyConfig} that asserts that there was no diff between any of the
     * expected responses set by simulations and the actual responses returned from the real service.
     * The rule also removes (before and after the test execution) all possible diffs that are stored in Hoverfly.
     * This ensures that all test runs are executed in isolated and clean environment.
     *
     * @param hoverflyConfig
     *     An instance of {@link HoverflyConfig} to be used for retrieving the diffs
     */
    public NoDiffAssertionRule(HoverflyConfig hoverflyConfig) {
        if (hoverflyConfig == null) {
            throw new IllegalArgumentException("HoverflyConfig cannot be null");
        }
        hoverfly = new Hoverfly(hoverflyConfig, HoverflyMode.DIFF);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                cleanDiffs();
                base.evaluate();
                performAssertion();
            }
        };
    }

    private void cleanDiffs() {
        if (hoverfly != null) {
            hoverfly.resetDiffs();
        } else {
            hoverflyRule.resetDiffs();
        }
    }

    private void performAssertion() {
        if (hoverfly != null) {
            hoverfly.assertThatNoDiffIsReported(true);
        } else {
            hoverflyRule.assertThatNoDiffIsReported();
        }
    }
}
