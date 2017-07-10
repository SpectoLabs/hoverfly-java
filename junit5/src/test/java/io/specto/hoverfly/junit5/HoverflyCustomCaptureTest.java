package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit5.api.HoverflyCapture;
import java.io.File;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HoverflyCustomCaptureTest {

    @Test
    public void shouldExportSimulationWithConfiguration() throws Exception {

        ExtensionContext extensionContext = mock(ExtensionContext.class);
        when(extensionContext.getTestClass()).thenReturn(Optional.of(MyTestClass.class));
        when(extensionContext.getElement()).thenReturn(Optional.of(MyTestClass.class));


        // Since afterAll event is the latest one we can hook, and it is when the simulation is exported
        // Instead of writing an it test testing the whole lifecycle, we just instantiate the class in test itself.
        // In this way we can check the existance of test

        HoverflyCaptureResolver hoverflyCaptureResolver = new HoverflyCaptureResolver();
        hoverflyCaptureResolver.beforeAll(extensionContext);
        hoverflyCaptureResolver.afterAll(extensionContext);

        assertThat(new File("build/resources/test/io_specto_hoverfly_junit5_HoverflyCustomCaptureTest_MyTestClass.json"))
            .exists();

    }

    @HoverflyCapture(path = "build/resources/test")
    static class MyTestClass {
    }

}
