package io.specto.hoverfly.junit.core;

public interface LocalHoverflyConfig extends HoverflyConfig {

    LocalHoverflyConfig sslCertificatePath(String sslCertificatePath);

    LocalHoverflyConfig sslKeyPath(String sslKeyPath);
}
