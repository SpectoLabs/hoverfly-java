package io.specto.hoverfly.junit.core;

public interface RemoteHoverflyConfig extends HoverflyConfig {


    RemoteHoverflyConfig withHttps();

    RemoteHoverflyConfig withAuthHeader();

    RemoteHoverflyConfig host(String host);

    RemoteHoverflyConfig withHttps(String sslCert);

    RemoteHoverflyConfig withAuthHeader(String authToken);


}
