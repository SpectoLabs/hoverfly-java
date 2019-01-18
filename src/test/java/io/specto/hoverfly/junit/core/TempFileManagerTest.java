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

import static org.assertj.core.api.Assertions.assertThat;

public class TempFileManagerTest {

    private TempFileManager tempFileManager;
    private String systemTempDir = System.getProperty("java.io.tmpdir");

    @Before
    public void setUp() {
        tempFileManager = new TempFileManager();
    }

    @Test
    public void shouldLazilyInitializedTempDirectory() {
        assertThat(tempFileManager.getTempDirectory()).isNull();

        tempFileManager.copyHoverflyBinary(new SystemConfigFactory().createSystemConfig());

        Path tempDir = tempFileManager.getTempDirectory();
        assertThat(Files.isDirectory(tempDir)).isTrue();
        assertThat(Files.isWritable(tempDir)).isTrue();
        assertThat(tempDir.getParent()).isEqualTo(Paths.get(systemTempDir));
    }


    @Test
    public void shouldPurgeAllCreatedTempFiles() {
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
        SystemConfig systemConfig = new SystemConfigFactory().createSystemConfig();
        URL sourceFileUrl = Resources.getResource("binaries/" + systemConfig.getHoverflyBinaryName());
        Path sourceFile = Paths.get(sourceFileUrl.toURI());

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
    public void tearDown() {
        tempFileManager.purge();
    }
}