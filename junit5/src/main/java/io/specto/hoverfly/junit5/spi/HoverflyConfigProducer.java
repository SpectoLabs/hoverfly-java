package io.specto.hoverfly.junit5.spi;

import io.specto.hoverfly.junit.core.HoverflyConfig;

/**
 * Interface to be implemented in case of want to configure Hoverfly instance with specific options.
 * For example SSL or Remote host
 *
 * @see HoverflyConfig
 */
public interface HoverflyConfigProducer {

    HoverflyConfig create();

    class DefaultHoverflyConfigProducer implements HoverflyConfigProducer {
        @Override
        public HoverflyConfig create() {
            return HoverflyConfig.configs();
        }
    }

}
