package io.specto.hoverfly.junit.core;

public interface RemoteHoverflyConfig extends HoverflyConfig {

    RemoteHoverflyConfig host(String host);

    RemoteHoverflyConfig withAuthHeader();

    RemoteHoverflyConfig withAuthHeader(String authToken);

    RemoteHoverflyConfig withHttpsAdminEndpoint();

    // TODO add support for custom server certificate for admin endpoint
}
