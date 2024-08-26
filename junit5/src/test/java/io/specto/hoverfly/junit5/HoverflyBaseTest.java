package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import io.specto.hoverfly.junit5.api.HoverflySimulate.Source;
import io.specto.hoverfly.junit5.api.HoverflySimulate.SourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({HoverflyExtension.class})
@HoverflySimulate(source = @Source(type = SourceType.EMPTY))
public abstract class HoverflyBaseTest {

  @BeforeEach
  void setUp(Hoverfly hoverfly) {
    hoverfly.reset();
    hoverfly.simulate(SimulationSource.classpath("test-service-https.json"));
  }

}
