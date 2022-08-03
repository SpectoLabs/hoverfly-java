package io.specto.hoverfly.junit.core;


import io.specto.hoverfly.junit.core.config.HoverflyConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static io.specto.hoverfly.junit.core.SystemConfigFactory.ArchType.*;
import static io.specto.hoverfly.junit.core.SystemConfigFactory.OsName.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SystemConfigFactoryTest {

    private SystemConfigFactory factory;
    private SystemInfo systemInfo;

    @Before
    public void setUp() {
        factory = new SystemConfigFactory();
        systemInfo = mock(SystemInfo.class);
        Whitebox.setInternalState(factory, "systemInfo", systemInfo);
    }

    @Test
    public void shouldCreateSystemConfigForWindows() {

        when(systemInfo.isOsWindows()).thenReturn(true);

        SystemConfig systemConfig = factory.createSystemConfig();

        assertThat(systemConfig.getOsName()).isEqualTo(WINDOWS);
    }

    @Test
    public void shouldCreateSystemConfigForLinux() {

        when(systemInfo.isOsLinux()).thenReturn(true);

        SystemConfig systemConfig = factory.createSystemConfig();

        assertThat(systemConfig.getOsName()).isEqualTo(LINUX);
    }

    @Test
    public void shouldCreateSystemConfigWithArm64BitArchType() {

        when(systemInfo.isOsLinux()).thenReturn(true);
        when(systemInfo.is64BitSystem()).thenReturn(true);
        when(systemInfo.isArmArchitecture()).thenReturn(true);

        SystemConfig systemConfig = factory.createSystemConfig();

        assertThat(systemConfig.getArchType()).isEqualTo(ARCH_ARM64);
    }

    @Test
    public void shouldCreateSystemConfigForMac() {

        when(systemInfo.isOsMac()).thenReturn(true);

        SystemConfig systemConfig = factory.createSystemConfig();

        assertThat(systemConfig.getOsName()).isEqualTo(OSX);
    }

    @Test
    public void shouldCreateSystemConfigWith32BitArchType() {

        when(systemInfo.isOsMac()).thenReturn(true);
        when(systemInfo.is64BitSystem()).thenReturn(false);

        SystemConfig systemConfig = factory.createSystemConfig();

        assertThat(systemConfig.getArchType()).isEqualTo(ARCH_386);
    }

    @Test
    public void shouldCreateSystemConfigWith64BitArchType() {

        when(systemInfo.isOsMac()).thenReturn(true);
        when(systemInfo.is64BitSystem()).thenReturn(true);

        SystemConfig systemConfig = factory.createSystemConfig();

        assertThat(systemConfig.getArchType()).isEqualTo(ARCH_AMD64);
    }

    @Test
    public void shouldThrowExceptionWhenOsTypeIsNotSupported() {

        when(systemInfo.isOsWindows()).thenReturn(false);
        when(systemInfo.isOsLinux()).thenReturn(false);
        when(systemInfo.isOsMac()).thenReturn(false);

        Throwable thrown = catchThrowable(() -> factory.createSystemConfig());

        assertThat(thrown).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void shouldBeAbleToUseBinaryNameFormatFromHoverflyConfiguration() {

        HoverflyConfiguration configs = mock(HoverflyConfiguration.class);
        Whitebox.setInternalState(factory, "configs", configs);

        when(configs.getBinaryNameFormat()).thenReturn("hoverfly2_%s_%s%s");
        when(systemInfo.isOsLinux()).thenReturn(true);
        when(systemInfo.is64BitSystem()).thenReturn(true);

        SystemConfig systemConfig = factory.createSystemConfig();

        assertThat(systemConfig.getHoverflyBinaryName()).isEqualTo("hoverfly2_linux_amd64");
    }
}