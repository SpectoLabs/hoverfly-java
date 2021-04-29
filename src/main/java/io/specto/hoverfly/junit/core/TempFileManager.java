package io.specto.hoverfly.junit.core;

import static io.specto.hoverfly.junit.core.SystemConfigFactory.OsName.WINDOWS;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage temporary files for running hoverfly
 */
class TempFileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TempFileManager.class);
    private static final String TEMP_DIR_PREFIX = "hoverfly.";
    private static final String HOVERFLY_BINARIES_ROOT_PATH = "binaries/";
    private Path tempDirectory;

    /**
     * Delete the hoverfly temporary directory recursively
     */
    void purge() {
        if (tempDirectory == null) {
            return;
        }
        try {
            Files.walk(tempDirectory)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        } catch (IOException e) {
            LOGGER.warn("Failed to delete hoverfly binary, will try again on JVM shutdown.", e);
        }
    }

    /**
     * Copy classpath resource to hoverfly temporary directory
     */
    Path copyClassPathResource(String resourcePath, String targetName) {

        Path targetPath = getOrCreateTempDirectory().resolve(targetName);
        try (InputStream resourceAsStream = HoverflyUtils.getClasspathResourceAsStream(resourcePath)) {
            Files.copy(resourceAsStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
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
        Path targetPath = getOrCreateTempDirectory().resolve(binaryName);
        LOGGER.info("Storing binary in temporary directory {}", targetPath);

        try (InputStream resourceAsStream = HoverflyUtils.getClasspathResourceAsStream(HOVERFLY_BINARIES_ROOT_PATH + binaryName)) {
            Files.copy(resourceAsStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            if (systemConfig.getOsName() == WINDOWS) {
                final File targetFile = targetPath.toFile();
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

    /**
     * Return the temporary directory as Path
     */
    Path getTempDirectory() {
        return tempDirectory;
    }

    /**
     * Get or create temporary directory
     */
    private Path getOrCreateTempDirectory() {
        if (tempDirectory == null) {

            try {
                tempDirectory = Files.createTempDirectory(TEMP_DIR_PREFIX);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create temp directory.", e);
            }
        }

        return tempDirectory;
    }

    void setBinaryLocation(String binaryLocation) {
        this.tempDirectory = Paths.get(binaryLocation).toAbsolutePath();
    }

}
