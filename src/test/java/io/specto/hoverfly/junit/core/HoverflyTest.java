package io.specto.hoverfly.junit.core;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.specto.hoverfly.junit.api.HoverflyClient;
import io.specto.hoverfly.junit.api.HoverflyClientException;
import io.specto.hoverfly.junit.api.model.ModeArguments;
import io.specto.hoverfly.junit.api.view.HoverflyInfoView;
import io.specto.hoverfly.junit.core.model.Simulation;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.powermock.reflect.Whitebox;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.StartedProcess;

import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.List;

import static io.specto.hoverfly.junit.core.HoverflyConfig.localConfigs;
import static io.specto.hoverfly.junit.core.HoverflyConfig.remoteConfigs;
import static io.specto.hoverfly.junit.core.HoverflyMode.*;
import static io.specto.hoverfly.junit.core.SimulationSource.classpath;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.OK;

public class HoverflyTest {

    private static final int EXPECTED_PROXY_PORT = 8890;
    private Hoverfly hoverfly;
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldStartHoverflyOnConfiguredPort() throws Exception {

        hoverfly = new Hoverfly(localConfigs().proxyPort(EXPECTED_PROXY_PORT), SIMULATE);
        hoverfly.start();
        assertThat(System.getProperty("http.proxyPort")).isEqualTo(String.valueOf(EXPECTED_PROXY_PORT));
        assertThat(hoverfly.getHoverflyConfig().getProxyPort()).isEqualTo(EXPECTED_PROXY_PORT);
    }

    @Test
    public void shouldDeleteTempFilesWhenStoppingHoverfly() throws Exception {
        // Given
        hoverfly = new Hoverfly(SIMULATE);
        TempFileManager tempFileManager = spy(TempFileManager.class);
        Whitebox.setInternalState(hoverfly, "tempFileManager", tempFileManager);

        // When
        hoverfly.close();

        // Then
        verify(tempFileManager).purge();
    }

    @Test
    public void shouldBeAbleToSetMode() {

        hoverfly = new Hoverfly(SIMULATE);
        hoverfly.start();

        hoverfly.setMode(SPY);

        assertThat(hoverfly.getMode()).isEqualTo(SPY);
    }

    @Test
    public void shouldImportSimulation() throws Exception {
        startDefaultHoverfly();
        // When
        URL resource = Resources.getResource("test-service.json");
        Simulation importedSimulation = mapper.readValue(resource, Simulation.class);
        hoverfly.simulate(classpath("test-service.json"));

        // Then
        Simulation exportedSimulation = hoverfly.getSimulation();
        assertThat(exportedSimulation.getHoverflyData()).isEqualTo(importedSimulation.getHoverflyData());
    }

    @Test
    public void shouldThrowExceptionWhenExportSimulationWithoutPath() throws Exception {

        hoverfly = new Hoverfly(CAPTURE);

        assertThatThrownBy(() -> hoverfly.exportSimulation(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    public void shouldThrowExceptionWhenProxyPortIsAlreadyInUse() throws Exception {
        // Given
        startDefaultHoverfly();

        try (Hoverfly portClashHoverfly = new Hoverfly(localConfigs().proxyPort(hoverfly.getHoverflyConfig().getProxyPort()), SIMULATE)) {
            // When
            Throwable throwable = catchThrowable(portClashHoverfly::start);

            //Then
            assertThat(throwable)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Port is already in use");
        }
    }

    @Test
    public void shouldThrowExceptionWhenAdminPortIsAlreadyInUse() throws Exception {
        // Given
        startDefaultHoverfly();

        try (Hoverfly portClashHoverfly = new Hoverfly(localConfigs().adminPort(hoverfly.getHoverflyConfig().getAdminPort()), SIMULATE)) {
            // When
            Throwable throwable = catchThrowable(portClashHoverfly::start);

            //Then
            assertThat(throwable)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Port is already in use");
        }
    }

    @Test
    public void shouldWarnWhenStartHoverflyInstanceTwice() {
        // Given
        // Reference: https://dzone.com/articles/unit-testing-asserting-line
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        final Appender mockAppender = mock(Appender.class);
        when(mockAppender.getName()).thenReturn("test-shouldWarnWhenStartHoverflyInstanceTwice");
        root.addAppender(mockAppender);

        startDefaultHoverfly();

        // when
        hoverfly.start();

        // then
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent event = (LoggingEvent) argument;
            return event.getLevel().levelStr.equals("WARN") &&
                    event.getMessage().contains("Local Hoverfly is already running");
        }));
    }


    @Test
    public void shouldNotOverrideDefaultTrustManager() throws Exception {
        startDefaultHoverfly();

        HttpClient client = HttpClientBuilder.create().setSSLContext(SSLContext.getDefault()).build();

        // TODO: Find better way to test trust store
        HttpResponse response = client.execute(new HttpGet("https://specto.io"));
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(OK.value());
    }


    @Test
    public void shouldWaitForHoverflyProcessTerminatedBeforeDeletingBinary() throws Exception {
        // Given
        hoverfly = new Hoverfly(SIMULATE);

        TempFileManager tempFileManager = spy(TempFileManager.class);
        Whitebox.setInternalState(hoverfly, "tempFileManager", tempFileManager);

        StartedProcess mockStartedProcess = mock(StartedProcess.class);
        Whitebox.setInternalState(hoverfly, "startedProcess", mockStartedProcess);
        Process mockProcess = mock(Process.class);
        when(mockStartedProcess.getProcess()).thenReturn(mockProcess);

        // When
        hoverfly.close();

        // Then
        InOrder inOrder = inOrder(mockProcess, tempFileManager);
        inOrder.verify(mockProcess).destroy();
        inOrder.verify(mockProcess).waitFor();
        inOrder.verify(tempFileManager).purge();
    }


    @Test
    public void shouldSetTrustStoreWhenStartingHoverfly() throws Exception {
        // Given
        hoverfly = new Hoverfly(SIMULATE);
        SslConfigurer sslConfigurer = mock(SslConfigurer.class);
        Whitebox.setInternalState(hoverfly, "sslConfigurer", sslConfigurer);

        // When
        hoverfly.start();

        // Then
        verify(sslConfigurer).setDefaultSslContext();
    }

    @Test
    public void shouldNotSetJVMTrustStoreIfSslCertificatePathExists() throws Exception {
        // Given
        hoverfly = new Hoverfly(localConfigs()
                .sslCertificatePath("ssl/ca.crt")
                .sslKeyPath("ssl/ca.key"), SIMULATE);
        SslConfigurer sslConfigurer = mock(SslConfigurer.class);
        Whitebox.setInternalState(hoverfly, "sslConfigurer", sslConfigurer);

        // When
        hoverfly.start();

        // Then
        verify(sslConfigurer, never()).setDefaultSslContext();
    }

    @Test
    public void shouldSetSslCertForRemoteInstance() throws Exception {

        hoverfly = new Hoverfly(remoteConfigs().host("remotehost").proxyCaCert("ssl/ca.crt"), SIMULATE);

        SslConfigurer sslConfigurer = mock(SslConfigurer.class);
        Whitebox.setInternalState(hoverfly, "sslConfigurer", sslConfigurer);

        HoverflyClient hoverflyClient = createMockHoverflyClient(hoverfly);

        when(hoverflyClient.getHealth()).thenReturn(true);

        // When
        hoverfly.start();

        // Then
        verify(sslConfigurer).setDefaultSslContext("ssl/ca.crt");
    }

    @Test
    public void shouldResetJournalWhenUsingARemoteHoverflyInstance() throws Exception {

        hoverfly = new Hoverfly(remoteConfigs(), SIMULATE);

        HoverflyClient hoverflyClient = createMockHoverflyClient(hoverfly);
        when(hoverflyClient.getHealth()).thenReturn(true);

        hoverfly.start();

        verify(hoverflyClient).deleteJournal();
    }

    @Test
    public void shouldCopySslCertAndKeyToTempFolderIfPresent () throws Exception {
        // Given
        hoverfly = new Hoverfly(localConfigs()
                .sslCertificatePath("ssl/ca.crt")
                .sslKeyPath("ssl/ca.key"), SIMULATE);
        TempFileManager tempFileManager = spy(TempFileManager.class);
        Whitebox.setInternalState(hoverfly, "tempFileManager", tempFileManager);

        // When
        hoverfly.start();

        // Then
        verify(tempFileManager).copyClassPathResource("ssl/ca.crt", "ca.crt");
        verify(tempFileManager).copyClassPathResource("ssl/ca.key", "ca.key");
    }

    @Test
    public void shouldCopyMiddlewareScriptToTempFolderIfLocalMiddlewareEnabled () throws Exception {
        // Given
        hoverfly = new Hoverfly(localConfigs()
           .localMiddleware("python", "middleware/middleware.py"), SIMULATE);
        TempFileManager tempFileManager = spy(TempFileManager.class);
        Whitebox.setInternalState(hoverfly, "tempFileManager", tempFileManager);

        // When
        hoverfly.start();

        // Then
        verify(tempFileManager).copyClassPathResource("middleware/middleware.py", "middleware.py");
    }


    @Test
    public void shouldCopyHoverflyBinaryToTempFolderOnStart() throws Exception {

        // Given
        hoverfly = new Hoverfly(SIMULATE);
        TempFileManager tempFileManager = spy(TempFileManager.class);
        Whitebox.setInternalState(hoverfly, "tempFileManager", tempFileManager);

        // When
        hoverfly.start();

        // Then
        verify(tempFileManager).copyHoverflyBinary(any(SystemConfig.class));
    }

    @Test
    public void shouldValidateHoverflyConfigBeforeStart() throws Exception {

        hoverfly = new Hoverfly(SIMULATE);

        assertThat(hoverfly.getHoverflyConfig().getProxyPort()).isNotZero();
        assertThat(hoverfly.getHoverflyConfig().getAdminPort()).isNotZero();
    }


    @Test
    public void shouldSetSystemPropertiesForLocalHoverflyInstance() throws Exception {

        startDefaultHoverfly();

        assertThat(System.getProperty("http.proxyHost")).isEqualTo("localhost");
        assertThat(System.getProperty("https.proxyHost")).isEqualTo("localhost");

        assertThat(System.getProperty("http.proxyPort")).isEqualTo(String.valueOf(hoverfly.getHoverflyConfig().getProxyPort()));
        assertThat(System.getProperty("https.proxyPort")).isEqualTo(String.valueOf(hoverfly.getHoverflyConfig().getProxyPort()));

        assertThat(System.getProperty("http.nonProxyHosts")).isEqualTo("localhost|127.*|[::1]");

    }

    @Test
    public void shouldSetNonProxyHostSystemPropertyToEmptyIfIsProxyLocalHost() throws Exception {
        hoverfly = new Hoverfly(localConfigs().proxyLocalHost(), SIMULATE);
        hoverfly.start();

        assertThat(System.getProperty("http.nonProxyHosts")).isEqualTo("");
    }

    @Test
    public void shouldNotSetSystemPropertiesWhenHoverflyInWebServerMode() {
        System.clearProperty("http.proxyHost");
        System.clearProperty("https.proxyHost");

        hoverfly = new Hoverfly(localConfigs().asWebServer(), SIMULATE);
        hoverfly.start();

        assertThat(System.getProperty("http.proxyHost")).isNullOrEmpty();
        assertThat(System.getProperty("https.proxyHost")).isNullOrEmpty();
    }

    @Test
    public void shouldBeAbleToUseHoverflyInTryWithResourceStatement() throws Exception {
        StartedProcess startedProcess = null;
        try (Hoverfly hoverfly = new Hoverfly(SIMULATE)) {

            hoverfly.start();
            startedProcess = Whitebox.getInternalState(hoverfly, "startedProcess");
        } finally {

            assertThat(startedProcess.getProcess().isAlive()).isFalse();
        }

    }

    @Test
    public void shouldSetHeadersForCaptureMode() throws Exception {
        hoverfly = new Hoverfly(localConfigs().captureHeaders("Authorization"), CAPTURE);

        HoverflyClient hoverflyClient = createMockHoverflyClient(hoverfly);
        when(hoverflyClient.getHealth()).thenReturn(true);

        hoverfly.start();

        ArgumentCaptor<ModeArguments> modeArgumentCaptor = ArgumentCaptor.forClass(ModeArguments.class);
        verify(hoverflyClient).setMode(eq(HoverflyMode.CAPTURE), modeArgumentCaptor.capture());

        List<String> headersWhitelist = modeArgumentCaptor.getValue().getHeadersWhitelist();
        assertThat(headersWhitelist).hasSize(1);
        assertThat(headersWhitelist).containsOnly("Authorization");
    }

    @Test
    public void shouldNotSetHeadersForNonCaptureMode() throws Exception {
        hoverfly = new Hoverfly(localConfigs().captureAllHeaders(), SIMULATE);

        HoverflyClient hoverflyClient = createMockHoverflyClient(hoverfly);
        when(hoverflyClient.getHealth()).thenReturn(true);

        hoverfly.start();

        verify(hoverflyClient, never()).setMode(eq(HoverflyMode.SIMULATE), any());
    }

    @Test
    public void shouldSetUpstreamProxy() {
        hoverfly = new Hoverfly(localConfigs().upstreamProxy(new InetSocketAddress("127.0.0.1", 8900)), SIMULATE);

        hoverfly.start();
        HoverflyInfoView hoverflyInfo = hoverfly.getHoverflyInfo();

        assertThat(hoverflyInfo.getUpstreamProxy()).isEqualTo("http://127.0.0.1:8900");
    }

    @Test
    public void shouldTolerateFailureOnResetJournal() throws Exception {

        hoverfly = new Hoverfly(SIMULATE);
        HoverflyClient hoverflyClient = createMockHoverflyClient(hoverfly);
        doThrow(HoverflyClientException.class).when(hoverflyClient).deleteJournal();

        hoverfly.resetJournal();

        verify(hoverflyClient).deleteJournal();
    }

    @Test
    public void shouldBeAbleToSetDiffMode() {
        // given
        hoverfly = new Hoverfly(SPY);
        hoverfly.start();

        // when
        hoverfly.setMode(DIFF);

        // then
        assertThat(hoverfly.getMode()).isEqualTo(DIFF);
    }

    @Test
    public void shouldImportExpectedSimulationInDiffMode() throws Exception {
        // given
        startDefaultHoverfly();
        URL resource = Resources.getResource("test-service.json");
        Simulation importedSimulation = mapper.readValue(resource, Simulation.class);

        // when
        hoverfly.simulate(classpath("test-service.json"));

        // then
        Simulation exportedSimulation = hoverfly.getSimulation();
        assertThat(exportedSimulation.getHoverflyData()).isEqualTo(importedSimulation.getHoverflyData());
    }

    @Test
    public void shouldTolerateFailureOnResetDiff() throws Exception {
        // given
        hoverfly = new Hoverfly(DIFF);
        HoverflyClient hoverflyClient = createMockHoverflyClient(hoverfly);
        doThrow(HoverflyClientException.class).when(hoverflyClient).cleanDiffs();

        // when
        hoverfly.resetDiffs();

        // then
        verify(hoverflyClient).cleanDiffs();
    }

    @After
    public void tearDown() throws Exception {
        if (hoverfly != null) {
            hoverfly.close();
        }
    }

    private HoverflyClient createMockHoverflyClient(Hoverfly hoverfly) {
        HoverflyClient hoverflyClient = mock(HoverflyClient.class);
        Whitebox.setInternalState(hoverfly, "hoverflyClient", hoverflyClient);
        return hoverflyClient;
    }

    private void startDefaultHoverfly() {
        hoverfly = new Hoverfly(SIMULATE);
        hoverfly.start();
    }

}
