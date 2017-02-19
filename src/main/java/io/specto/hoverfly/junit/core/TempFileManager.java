package io.specto.hoverfly.junit.core;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

import static io.specto.hoverfly.junit.core.HoverflyUtils.findResourceOnClasspath;
import static io.specto.hoverfly.junit.core.SystemConfigFactory.OsName.WINDOWS;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.util.Arrays.asList;

class TempFileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TempFileManager.class);
    private static final String TEMP_DIR_PREFIX = "hoverfly.";
    private static final String HOVERFLY_BINARIES_ROOT_PATH = "binaries/";
    private Path tempDirectory;

    void purge() {
        if (tempDirectory == null) {
            return;
        }
        try {
            FileUtils.deleteDirectory(tempDirectory.toFile());
        } catch (IOException e) {
            LOGGER.warn("Failed to delete hoverfly binary, will try again on JVM shutdown.", e);
        }

    }

    Path copyClassPathResource(String resourcePath, String targetName) {
        URL sourceUrl = HoverflyUtils.findResourceOnClasspath(resourcePath);

        Path targetPath = getOrCreateTempDirecotry().resolve(targetName);
        try {
            FileUtils.copyURLToFile(sourceUrl, targetPath.toFile());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to copy classpath resource " + resourcePath, e);
        }

        return targetPath;
    }

    /**
     * Extracts and runs the binary, setting any appropriate permissions.
     *
     */
    Path copyHoverflyBinary(SystemConfig systemConfig) {
        String binaryName = systemConfig.getHoverflyBinaryName();
        LOGGER.info("Selecting the following binary based on the current operating system: {}", binaryName);
        final URL sourceUrl = findResourceOnClasspath(HOVERFLY_BINARIES_ROOT_PATH + binaryName);
        final Path targetPath = getOrCreateTempDirecotry().resolve(binaryName);
        LOGGER.info("Storing binary in temporary directory {}", targetPath);
        final File targetFile = targetPath.toFile();
        try {
            FileUtils.copyURLToFile(sourceUrl, targetFile);
            if (systemConfig.getOsName() == WINDOWS) {
                targetFile.setExecutable(true);
                targetFile.setReadable(true);
                targetFile.setWritable(true);
            } else {
                Files.setPosixFilePermissions(targetPath, new HashSet<>(asList(OWNER_EXECUTE, OWNER_READ)));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to copy hoverfly binary.", e);
        }

        return targetPath;
    }

    Path getTempDirectory() {
        return tempDirectory;
    }

    private Path getOrCreateTempDirecotry() {
        if (tempDirectory == null) {

            try {
                tempDirectory = Files.createTempDirectory(TEMP_DIR_PREFIX);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create temp directory.", e);
            }
        }

        return tempDirectory;
    }

}
