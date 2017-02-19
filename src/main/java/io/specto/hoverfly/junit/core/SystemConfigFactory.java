package io.specto.hoverfly.junit.core;

class SystemConfigFactory {

    private SystemInfo systemInfo = new SystemInfo();

    SystemConfig createSystemConfig() {

        SystemConfig systemConfig = new SystemConfig();

        if (systemInfo.isOsWindows()) {
           systemConfig.setOsName(OsName.WINDOWS);
        } else if (systemInfo.isOsLinux()) {
            systemConfig.setOsName(OsName.LINUX);
        } else if (systemInfo.isOsMac()) {
            systemConfig.setOsName(OsName.OSX);
        } else {
            throw new UnsupportedOperationException(systemInfo.getOsName() + " is not currently supported");
        }

        if (systemInfo.is64BitSystem()) {
            systemConfig.setArchType(ArchType.ARCH_AMD64);
        } else {
            systemConfig.setArchType(ArchType.ARCH_386);
        }

        return systemConfig;
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
