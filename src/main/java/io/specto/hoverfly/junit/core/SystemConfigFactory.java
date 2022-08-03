package io.specto.hoverfly.junit.core;

import io.specto.hoverfly.junit.core.config.HoverflyConfiguration;

/**
 * Create platform specific configuration based on system info
 */
class SystemConfigFactory {

    static final String DEFAULT_BINARY_NAME_FORMAT = "hoverfly_%s_%s%s";

    private final SystemInfo systemInfo = new SystemInfo();
    private HoverflyConfiguration configs;

    SystemConfigFactory() {
    }

    SystemConfigFactory(HoverflyConfiguration configs) {
        this.configs = configs;
    }

    SystemConfig createSystemConfig() {

        OsName osName;
        ArchType archType;
        String binaryNameFormat = DEFAULT_BINARY_NAME_FORMAT;

        if (systemInfo.isOsWindows()) {
           osName = OsName.WINDOWS;
        } else if (systemInfo.isOsLinux()) {
            osName = OsName.LINUX;
        } else if (systemInfo.isOsMac()) {
            osName = OsName.OSX;
        } else {
            throw new UnsupportedOperationException(systemInfo.getOsName() + " is not currently supported");
        }

        if (systemInfo.is64BitSystem()) {
            if (systemInfo.isOsLinux() && systemInfo.isArmArchitecture()) {
                archType = ArchType.ARCH_ARM64;
            } else {
                archType = ArchType.ARCH_AMD64;
            }
        } else {
            archType = ArchType.ARCH_386;
        }

        if (configs != null && configs.getBinaryNameFormat() != null) {
            binaryNameFormat = configs.getBinaryNameFormat();
        }

        return new SystemConfig(osName, archType, binaryNameFormat);
    }

    enum OsName {
        OSX("OSX"),
        WINDOWS("windows"),
        LINUX("linux");


        private final String name;
        OsName(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }
    }

    enum ArchType {
        ARCH_AMD64("amd64"),
        ARCH_ARM64("arm64"),
        ARCH_386("386");

        private final String name;
        ArchType(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }
    }

}
