package io.specto.hoverfly.junit5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.specto.hoverfly.junit.core.config.HoverflyConfiguration;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.UnsetSimulationPreprocessor;
import org.junit.jupiter.api.Test;

class HoverflyExtensionUtilsTest {

  @Test
  void shouldBeAbleToCustomizeBinaryLocation() {

    // testing with mock file directory is easier
    HoverflyConfig mockHoverflyConfig = mock(HoverflyConfig.class);
    // mock default values
    when(mockHoverflyConfig.remoteHost()).thenReturn("");
    when(mockHoverflyConfig.captureHeaders()).thenReturn(new String[]{});
    when(mockHoverflyConfig.commands()).thenReturn(new String[]{});
    when(mockHoverflyConfig.destination()).thenReturn(new String[]{});
    doReturn(UnsetSimulationPreprocessor.class).when(mockHoverflyConfig).simulationPreprocessor();
    when(mockHoverflyConfig.binaryLocation()).thenReturn("/home/hoverfly");

    HoverflyConfiguration actualConfigs = HoverflyExtensionUtils.getHoverflyConfigs(mockHoverflyConfig).build();

    assertThat(actualConfigs.getBinaryLocation()).isEqualTo("/home/hoverfly");
  }
}
