package io.specto.hoverfly.junit.api;

import io.specto.hoverfly.junit.core.HoverflyConstants;
import io.specto.hoverfly.junit.core.config.HoverflyConfiguration;

public class HoverflyOkHttpClientFactory implements HoverflyClientFactory {

    public HoverflyClient createHoverflyClient(HoverflyConfiguration hoverflyConfig) {
        return custom()
                .scheme(hoverflyConfig.getScheme())
                .host(hoverflyConfig.getHost())
                .port(hoverflyConfig.getAdminPort())
                .withAuthToken()
                .build();
    }

    /**
     * Static factory method for creating a {@link Builder}
     * @return a builder for HoverflyClient
     */
    static Builder custom() {
        return new Builder();
    }

    /**
     * Static factory method for default Hoverfly client
     * @return a default HoverflyClient
     */
    static HoverflyClient createDefault() {
        return new Builder().build();
    }

    /**
     * HTTP client builder for Hoverfly admin API
     */
    static class Builder {

        private String scheme = HoverflyConstants.HTTP;
        private String host = HoverflyConstants.LOCALHOST;
        private int port = HoverflyConstants.DEFAULT_ADMIN_PORT;
        private String authToken = null;

        Builder() {
        }

        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Get token from environment variable "HOVERFLY_AUTH_TOKEN" to authenticate with admin API
         * @return this Builder for further customizations
         */
        public Builder withAuthToken() {
            this.authToken = System.getenv(HoverflyConstants.HOVERFLY_AUTH_TOKEN);
            return this;
        }

        public HoverflyClient build() {
            return new OkHttpHoverflyClient(scheme, host, port, authToken);
        }
    }

}
