package io.specto.hoverfly.junit.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static io.specto.hoverfly.junit.core.HoverflyConfig.configs;
import static io.specto.hoverfly.junit.core.HoverflyMode.SIMULATE;
import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class RemoteHoverflyTest {

    private Hoverfly remoteHoverflyStub;

    @Before
    public void setUp() throws Exception {
        remoteHoverflyStub = new Hoverfly(SIMULATE);
        remoteHoverflyStub.start();

        remoteHoverflyStub.importSimulation(dsl(
                service("http://hoverfly-cloud:8888")
                        .get("/api/stats")
                        .willReturn(success())
        ));
    }

    @Test
    public void shouldSetSystemPropertiesForRemoteHoverflyInstance() throws Exception {

        // Given
        try (Hoverfly hoverflyUnderTest = new Hoverfly(configs().useRemoteInstance("hoverfly-cloud"), SIMULATE)) {

            // When
            hoverflyUnderTest.start();

            // Then
            assertThat(System.getProperty("http.proxyHost")).isEqualTo("hoverfly-cloud");
            assertThat(System.getProperty("https.proxyHost")).isEqualTo("hoverfly-cloud");

            assertThat(System.getProperty("http.proxyPort")).isEqualTo(String.valueOf(hoverflyUnderTest.getHoverflyConfig().getProxyPort()));
            assertThat(System.getProperty("https.proxyPort")).isEqualTo(String.valueOf(hoverflyUnderTest.getHoverflyConfig().getProxyPort()));

            assertThat(System.getProperty("http.nonProxyHosts")).isEqualTo("local|*.local|169.254/16|*.169.254/16|hoverfly-cloud");
        }
    }

    @Test
    public void shouldSetNonProxyHostsWhenUsingBothRemoteHoverflyInstanceAndProxyLocalHost() throws Exception {

        // Given
        try (Hoverfly hoverflyUnderTest = new Hoverfly(configs().useRemoteInstance("hoverfly-cloud").proxyLocalHost(true), SIMULATE)) {

            // When
            hoverflyUnderTest.start();

            // Then
            assertThat(System.getProperty("http.nonProxyHosts")).isEqualTo("hoverfly-cloud");
        }
    }

    @Test
    public void shouldNotInvokeTempFileManagerWhenUsingRemoteHoverfly() throws Exception {

        // Given
        try (Hoverfly hoverflyUnderTest = new Hoverfly(configs().useRemoteInstance("hoverfly-cloud"), SIMULATE)) {
            TempFileManager tempFileManager = mock(TempFileManager.class);
            Whitebox.setInternalState(hoverflyUnderTest, "tempFileManager", tempFileManager);

            // When
            hoverflyUnderTest.start();

            // Then
            verifyZeroInteractions(tempFileManager);
        }
    }

    @After
    public void tearDown() throws Exception {

        if(remoteHoverflyStub != null) {
            remoteHoverflyStub.close();
        }

    }
}
