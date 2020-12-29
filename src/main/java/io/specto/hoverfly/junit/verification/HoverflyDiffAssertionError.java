package io.specto.hoverfly.junit.verification;

import io.specto.hoverfly.junit.api.view.DiffView;

public class HoverflyDiffAssertionError extends AssertionError {

  private final DiffView diffs;

  public HoverflyDiffAssertionError(String message, DiffView diffs) {
    super(message);
    this.diffs = diffs;
  }

  public DiffView getDiffs() {
    return diffs;
  }
}
