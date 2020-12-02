package io.specto.hoverfly.junit.core;

import io.specto.hoverfly.junit.api.command.ModeCommand;
import io.specto.hoverfly.junit.api.view.HoverflyInfoView;
import io.specto.hoverfly.junit.dsl.HttpBodyConverter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static io.specto.hoverfly.junit.core.HoverflyConfig.remoteConfigs;
import static io.specto.hoverfly.junit.core.HoverflyMode.SIMULATE;
import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class RemoteHoverflyStubTest {

    private Hoverfly remoteHoverflyStub;

    @Before
    public void setUp() {
        remoteHoverflyStub = new Hoverfly(SIMULATE);
        remoteHoverflyStub.start();

        remoteHoverflyStub.simulate(dsl(
                service("http://hoverfly-cloud:8888")
                        .get("/api/health")
                        .willReturn(success())

                        .get("/api/v2/hoverfly")
                        .willReturn(success().body(HttpBodyConverter.json(new HoverflyInfoView(null, SIMULATE.getValue(), null, null, null, null, "v1.0.0"))))

                        .put("/api/v2/hoverfly/mode")
                        .body(HttpBodyConverter.json(new ModeCommand(SIMULATE)))
                        .willReturn(success())
        ));
    }

    @Test
    public void shouldSetSystemPropertiesForRemoteHoverflyInstance() {

        // Given
        try (Hoverfly hoverflyUnderTest = new Hoverfly(remoteConfigs().host("hoverfly-cloud"), SIMULATE)) {

            // When
            hoverflyUnderTest.start();

            // Then
            assertThat(System.getProperty("http.proxyHost")).isEqualTo("hoverfly-cloud");
            assertThat(System.getProperty("https.proxyHost")).isEqualTo("hoverfly-cloud");

            assertThat(System.getProperty("http.proxyPort")).isEqualTo(String.valueOf(hoverflyUnderTest.getHoverflyConfig().getProxyPort()));
            assertThat(System.getProperty("https.proxyPort")).isEqualTo(String.valueOf(hoverflyUnderTest.getHoverflyConfig().getProxyPort()));

            assertThat(System.getProperty("http.nonProxyHosts")).isEqualTo("localhost|127.*|[::1]|hoverfly-cloud");
        }
    }

    @Test
    public void shouldSetNonProxyHostsWhenUsingBothRemoteHoverflyInstanceAndProxyLocalHost() {

        // Given
        try (Hoverfly hoverflyUnderTest = new Hoverfly(remoteConfigs().host("hoverfly-cloud").proxyLocalHost(), SIMULATE)) {

            // When
            hoverflyUnderTest.start();

            // Then
            assertThat(System.getProperty("http.nonProxyHosts")).isEqualTo("hoverfly-cloud");
        }
    }

    @Test
    public void shouldNotInvokeTempFileManagerWhenUsingRemoteHoverfly() {

        // Given
        try (Hoverfly hoverflyUnderTest = new Hoverfly(remoteConfigs().host("hoverfly-cloud"), SIMULATE)) {
            TempFileManager tempFileManager = mock(TempFileManager.class);
            Whitebox.setInternalState(hoverflyUnderTest, "tempFileManager", tempFileManager);

            // When
            hoverflyUnderTest.start();

            // Then
            verifyZeroInteractions(tempFileManager);
        }
    }

    @After
    public void tearDown() {

        if(remoteHoverflyStub != null) {
            remoteHoverflyStub.close();
        }

    }
}
