package io.specto.hoverfly.junit.core;

import org.apache.commons.lang3.SystemUtils;

class SystemInfo {

    private final boolean isOsWindows = SystemUtils.IS_OS_WINDOWS;
    private final boolean isOsMac = SystemUtils.IS_OS_MAC;
    private final boolean isOsLinux = SystemUtils.IS_OS_LINUX;
    private final boolean is64BitSystem = SystemUtils.OS_ARCH.contains("64");
    private final String osName = SystemUtils.OS_NAME;

    boolean isOsWindows() {
        return isOsWindows;
    }

    boolean isOsMac() {
        return isOsMac;
    }

    boolean isOsLinux() {
        return isOsLinux;
    }

    boolean is64BitSystem() {
        return is64BitSystem;
    }

    String getOsName() {
        return osName;
    }
}
