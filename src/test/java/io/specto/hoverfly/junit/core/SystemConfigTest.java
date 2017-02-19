package io.specto.hoverfly.junit.core;


import org.junit.Test;

import static io.specto.hoverfly.junit.core.SystemConfigFactory.ArchType.ARCH_386;
import static io.specto.hoverfly.junit.core.SystemConfigFactory.ArchType.ARCH_AMD64;
import static io.specto.hoverfly.junit.core.SystemConfigFactory.OsName.LINUX;
import static io.specto.hoverfly.junit.core.SystemConfigFactory.OsName.WINDOWS;
import static org.assertj.core.api.Assertions.assertThat;

public class SystemConfigTest {

    private SystemConfig systemConfig;

    @Test
    public void shouldGetHoverflyBinaryNameForHostSystem() throws Exception {

        systemConfig = new SystemConfig(LINUX, ARCH_AMD64);

        assertThat(systemConfig.getHoverflyBinaryName()).isEqualTo("hoverfly_linux_amd64");
    }

    @Test
    public void shouldGeHoverlyBinaryNameForWindowsSystem() throws Exception {

        systemConfig = new SystemConfig(WINDOWS, ARCH_386);

        assertThat(systemConfig.getHoverflyBinaryName()).isEqualTo("hoverfly_windows_386.exe");

    }
}