package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit5.api.HoverflyCore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(HoverflyCoreResolver.class)
public class HoverflyCoreStaticTestInstanceTest {

    @HoverflyCore
    public static Hoverfly hoverfly;

    @Test
    public void shouldInjectHoverflyAndStartItAutomatically() {
        assertThat(hoverfly.getHoverflyInfo()).isNotNull();
    }

    @Test
    public void shouldInjectHoverflyParameterBeingTheSameAsStatic(@HoverflyCore(mode = HoverflyMode.CAPTURE) Hoverfly hoverfly) {
        assertThat(hoverfly).isEqualTo(HoverflyCoreStaticTestInstanceTest.hoverfly);
        assertThat(hoverfly.getMode()).isEqualTo(HoverflyMode.SIMULATE);
    }

    @AfterAll
    public static void checkHoverflyIsStillUp() {
        // AfterAll is executed before AfterAll callback
        assertThat(hoverfly.getHoverflyInfo()).isNotNull();
    }

}
