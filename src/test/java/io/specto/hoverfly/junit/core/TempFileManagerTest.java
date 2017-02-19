package io.specto.hoverfly.junit.core;


import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.specto.hoverfly.junit.core.SystemConfigFactory.OsName.OSX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TempFileManagerTest {

    private TempFileManager tempFileManager;
    private String systemTempDir = System.getProperty("java.io.tmpdir");

    @Before
    public void setUp() throws Exception {
        tempFileManager = new TempFileManager();
    }

    @Test
    public void shouldLazilyInitializedTempDirectory() throws Exception {
        assertThat(tempFileManager.getTempDirectory()).isNull();

        tempFileManager.copyHoverflyBinary(new SystemConfigFactory().createSystemConfig());

        Path tempDir = tempFileManager.getTempDirectory();
        assertThat(Files.isDirectory(tempDir)).isTrue();
        assertThat(Files.isWritable(tempDir)).isTrue();
        assertThat(tempDir.getParent().toString() + "/").isEqualTo(systemTempDir);
    }


    @Test
    public void shouldPurgeAllCreatedTempFiles() throws Exception {
        Path tempResourcePath = tempFileManager.copyClassPathResource("ssl/ca.crt", "ca.crt");

        tempFileManager.purge();

        assertThat(Files.exists(tempResourcePath)).isFalse();
        assertThat(Files.exists(tempResourcePath.getParent())).isFalse();
    }

    @Test
    public void shouldCopyClassPathResourceToCurrentTempDirectory() throws Exception {

        URL sourceFileUrl = Resources.getResource("ssl/ca.crt");
        Path sourceFile = Paths.get(sourceFileUrl.toURI());
        Path targetFile = tempFileManager.copyClassPathResource("ssl/ca.crt", "ca.crt");

        assertThat(Files.exists(targetFile)).isTrue();
        assertThat(Files.isRegularFile(targetFile)).isTrue();
        assertThat(Files.isReadable(targetFile)).isTrue();
        assertThat(targetFile.getParent()).isEqualTo(tempFileManager.getTempDirectory());
        assertThat(FileUtils.contentEquals(sourceFile.toFile(), targetFile.toFile())).isTrue();
    }

    @Test
    public void shouldCopyHoverflyBinary() throws Exception {

        // Given
        URL sourceFileUrl = Resources.getResource("binaries/hoverfly_OSX_amd64");
        Path sourceFile = Paths.get(sourceFileUrl.toURI());
        SystemConfig systemConfig = mock(SystemConfig.class);
        when(systemConfig.getHoverflyBinaryName()).thenReturn("hoverfly_OSX_amd64");
        when(systemConfig.getOsName()).thenReturn(OSX);

        // When
        Path targetFile = tempFileManager.copyHoverflyBinary(systemConfig);

        // Then
        assertThat(Files.exists(targetFile)).isTrue();
        assertThat(Files.isRegularFile(targetFile)).isTrue();
        assertThat(Files.isReadable(targetFile)).isTrue();
        assertThat(Files.isExecutable(targetFile)).isTrue();
        assertThat(targetFile.getParent()).isEqualTo(tempFileManager.getTempDirectory());
        assertThat(FileUtils.contentEquals(sourceFile.toFile(), targetFile.toFile())).isTrue();

    }

    @After
    public void tearDown() throws Exception {
        tempFileManager.purge();

    }
}