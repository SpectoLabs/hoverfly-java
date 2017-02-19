package io.specto.hoverfly.junit.core;

import org.apache.commons.lang3.SystemUtils;

class SystemInfo {

    private final boolean isOsWindows = SystemUtils.IS_OS_WINDOWS;
    private final boolean isOsMac = SystemUtils.IS_OS_MAC;
    private final boolean isOsLinux = SystemUtils.IS_OS_LINUX;
    private final boolean is64BitSystem = SystemUtils.OS_ARCH.contains("64");
    private final String osName = SystemUtils.OS_NAME;

    public boolean isOsWindows() {
        return isOsWindows;
    }

    public boolean isOsMac() {
        return isOsMac;
    }

    public boolean isOsLinux() {
        return isOsLinux;
    }

    public boolean is64BitSystem() {
        return is64BitSystem;
    }

    public String getOsName() {
        return osName;
    }
}
