package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.SimulationPreprocessor;
import io.specto.hoverfly.junit.core.model.Simulation;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflyCore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

class HoverflyExtensionErrorHandlingTest {
  @BeforeAll
  static void setUpAll() {
    System.setProperty("engine.test.kit", "enabled");
  }

  @AfterAll
  static void tearDownAll() {
    System.setProperty("engine.test.kit", "");
  }

  @Test
  void shouldFailOnInvalidSimulationPreprocessor() {
    Events events = EngineTestKit
            .engine("junit-jupiter")
            .selectors(selectClass(ShouldFailOnInvalidSimulationPreprocessor.class))
            .execute()
            .containers();
    events.assertStatistics(stats -> stats.started(2).finished(2));
    events.assertThatEvents()
            .extracting(e -> e.getPayload(TestExecutionResult.class))
            .describedAs("IllegalArgumentException should be thrown because of non-static inner class for SimulationPreprocessor.")
            .anySatisfy(r -> {
      assertTrue(r.isPresent());
      Optional<Throwable> throwable = r.get().getThrowable();
      assertTrue(throwable.isPresent());
      assertEquals(IllegalArgumentException.class, throwable.get().getClass());
    });
  }

  @HoverflyCore(mode = HoverflyMode.SIMULATE, config = @HoverflyConfig(
          simulationPreprocessor = InvalidSimulationPreprocessor.class
  ))
  // Workaround for https://github.com/junit-team/junit5/issues/1779
  @EnabledIfSystemProperty(named = "engine.test.kit", matches = "enabled")
  @ExtendWith(HoverflyExtension.class)
  static class ShouldFailOnInvalidSimulationPreprocessor {
    @Test
    void shouldFailOnInvalidSimulationPreprocessor() {
    }
  }

  class InvalidSimulationPreprocessor implements SimulationPreprocessor {
    @Override
    public void accept(Simulation simulation) {

    }
  }
}
