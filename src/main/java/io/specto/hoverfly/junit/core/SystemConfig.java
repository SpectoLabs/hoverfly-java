package io.specto.hoverfly.junit.core;

import io.specto.hoverfly.junit.core.SystemConfigFactory.ArchType;
import io.specto.hoverfly.junit.core.SystemConfigFactory.OsName;

class SystemConfig {

    private static final String BINARY_PATH_FORMAT = "hoverfly_%s_%s%s";

    private OsName osName;
    private ArchType archType;

    SystemConfig(OsName osName, ArchType archType) {
        this.osName = osName;
        this.archType = archType;
    }

    SystemConfig() { }

    OsName getOsName() {
        return osName;
    }

    void setOsName(OsName osName) {
        this.osName = osName;
    }

    ArchType getArchType() {
        return archType;
    }

    void setArchType(ArchType archType) {
        this.archType = archType;
    }

    /**
     * Calculates the binary to used based on OS and architecture
     */
    String getHoverflyBinaryName() {
        String extension = "";
        if (osName == OsName.WINDOWS) {
            extension = ".exe";
        }
        return String.format(BINARY_PATH_FORMAT, osName.getName(), archType.getName(), extension);
    }
}
