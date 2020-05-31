package io.specto.hoverfly.junit.api;

import io.specto.hoverfly.junit.core.config.HoverflyConfiguration;

/**
 * Factory for creating {@link HoverflyClient}s
 */
public interface HoverflyClientFactory {

    HoverflyClient createHoverflyClient(HoverflyConfiguration hoverflyConfig);

}
